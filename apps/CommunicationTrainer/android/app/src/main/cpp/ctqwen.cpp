// D04.1 experimental native CPU-only bridge. Pinned upstream version in CI.
// No networking, no disk writes, no use of Room/chat records.
// The Java caller holds the read-only SAF descriptor open throughout this call.
#include <jni.h>
#include <llama.h>
#include <algorithm>
#include <chrono>
#include <cstdint>
#include <exception>
#include <iomanip>
#include <mutex>
#include <sstream>
#include <string>
#include <vector>
#include <sys/stat.h>
#include <unistd.h>

namespace {
std::mutex bench_mutex;
using Clock = std::chrono::steady_clock;

std::string run(int fd, int n_threads, int n_ctx, int max_tokens) {
    if (fd < 0 || n_threads != 2 || n_ctx != 1024 || max_tokens != 48) {
        return "ABBRUCH: Ungültige Benchmarkparameter.";
    }
    struct stat details {};
    if (fstat(fd, &details) != 0 || !S_ISREG(details.st_mode) ||
        details.st_size < 300LL * 1024 * 1024 ||
        details.st_size > 600LL * 1024 * 1024) {
        return "ABBRUCH: Kein lesbarer regulärer GGUF-Dateideskriptor auf SD.";
    }
    // No hidden internal copy. May fail if the document provider disallows
    // opening /proc/self/fd/N. That is an explicit negative test outcome.
    const std::string path = "/proc/self/fd/" + std::to_string(fd);
    llama_backend_init();

    struct Resources {
        llama_model *model = nullptr;
        llama_context *ctx = nullptr;
        llama_sampler *sampler = nullptr;
        llama_batch_ext *batch = nullptr;
        ~Resources() {
            if (batch) llama_batch_ext_free(batch);
            if (sampler) llama_sampler_free(sampler);
            if (ctx) llama_free(ctx);
            if (model) llama_model_free(model);
            llama_backend_free();
        }
    } resources;

    llama_model_params mp = llama_model_default_params();
    mp.n_gpu_layers = 0;

    auto started = Clock::now();
    resources.model = llama_model_load_from_file(path.c_str(), mp);
    if (!resources.model) {
        return "ABBRUCH: GGUF konnte nicht direkt aus dem Dateideskriptor geladen werden. "
               "Keine interne Kopie und kein Cloud-Fallback.";
    }
    const llama_vocab *vocab = llama_model_get_vocab(resources.model);
    // Only a fixed synthetic prompt; no access to personal chat messages.
    const std::string prompt =
        "<|im_start|>system\n"
        "Du bist ein deutschsprachiger Testassistent. /no_think\n"
        "Antworte mit genau einem kurzen, freundlichen Satz.\n"
        "<|im_end|>\n"
        "<|im_start|>user\n"
        "/no_think Begruesse eine neue Person freundlich.\n"
        "<|im_end|>\n"
        "<|im_start|>assistant\n";

    const int count = -llama_tokenize(vocab, prompt.data(), prompt.size(), nullptr, 0, true, true);
    if (count <= 0 || count > 900) {
        return "ABBRUCH: Ungültige Tokenisierung.";
    }
    std::vector<llama_token> tokens(count);
    if (llama_tokenize(vocab, prompt.data(), prompt.size(), tokens.data(), count, true, true) < 0) {
        return "ABBRUCH: Prompt konnte nicht tokenisiert werden.";
    }

    llama_context_params cp = llama_context_default_params();
    cp.n_ctx = n_ctx;
    cp.n_batch = std::max(128, count);
    cp.n_threads = n_threads;
    cp.n_threads_batch = n_threads;
    cp.no_perf = false;
    resources.ctx = llama_init_from_model(resources.model, cp);
    if (!resources.ctx) return "ABBRUCH: Kontexterstellung wegen Ressourcen- oder Laufzeitproblem fehlgeschlagen.";

    resources.sampler = llama_sampler_chain_init(llama_sampler_chain_default_params());
    llama_sampler_chain_add(resources.sampler, llama_sampler_init_greedy());
    resources.batch = llama_batch_ext_init(resources.ctx);
    if (!resources.batch) return "ABBRUCH: Batch nicht verfügbar.";

    const auto fill_batch = [&](const llama_token *ids, int32_t number, llama_pos start) {
        llama_batch_ext_clear(resources.batch);
        for (int32_t i = 0; i < number; ++i) {
            int32_t index = llama_batch_ext_add_token(resources.batch, 0, ids[i]);
            const llama_pos position = start + i;
            llama_batch_ext_set_pos(resources.batch, index, &position);
        }
        llama_batch_ext_set_output_logits(resources.batch, number - 1, true);
    };

    fill_batch(tokens.data(), count, 0);
    if (llama_process(resources.ctx, LLAMA_PROCESS_TYPE_DECODE, resources.batch) != 0) {
        return "ABBRUCH: Prompt-Verarbeitung fehlgeschlagen.";
    }
    auto first = Clock::now();
    std::string answer;
    int generated = 0;
    int32_t position = count;
    for (; generated < max_tokens && position < n_ctx; ++position) {
        llama_token token = llama_sampler_sample(resources.sampler, resources.ctx, -1);
        if (llama_vocab_is_eog(vocab, token)) break;
        char piece[512];
        int n = llama_token_to_piece(vocab, token, piece, sizeof(piece), 0, true);
        if (n < 0) return "ABBRUCH: Ausgabe konnte nicht dekodiert werden.";
        answer.append(piece, n);
        ++generated;
        if (generated == max_tokens) break;
        fill_batch(&token, 1, position);
        if (llama_process(resources.ctx, LLAMA_PROCESS_TYPE_DECODE, resources.batch) != 0) {
            return "ABBRUCH: Token-Generierung fehlgeschlagen.";
        }
    }
    auto finished = Clock::now();
    const auto ms = [](auto duration) {
        return std::chrono::duration_cast<std::chrono::milliseconds>(duration).count();
    };
    const auto total_ms = ms(finished - started);
    const auto load_and_prefill_ms = ms(first - started);
    const auto decode_ms = ms(finished - first);
    std::ostringstream report;
    report << "ECHTE LOKALE CPU-MESSUNG (ein synthetischer Prompt)\n"
           << "Modellladen + Prompt: " << load_and_prefill_ms << " ms\n"
           << "Gesamt: " << total_ms << " ms\n"
           << "Ausgabetoken: " << generated << "\n"
           << "Dekodiergeschwindigkeit: " << std::fixed << std::setprecision(1)
           << (decode_ms > 0 ? (generated * 1000.0 / decode_ms) : 0.0) << " Token/s\n"
           << "Ausgabetext (ungeprüfte Modellqualität):\n" << answer;
    return report.str();
}
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_valoon4_communicationtrainer_inference_QwenNative_runBenchmark(
    JNIEnv *env, jobject, jint fd, jint threads, jint contextTokens, jint maxTokens) {
    std::lock_guard<std::mutex> lock(bench_mutex);
    std::string report;
    try {
        report = run(fd, threads, contextTokens, maxTokens);
    } catch (const std::exception &e) {
        report = std::string("ABBRUCH: Native Fehlermeldung: ") + e.what();
    } catch (...) {
        report = "ABBRUCH: Unbekannter nativer Fehler.";
    }
    return env->NewStringUTF(report.c_str());
}
