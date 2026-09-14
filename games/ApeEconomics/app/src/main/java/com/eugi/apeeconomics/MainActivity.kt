package com.eugi.apeeconomics

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import kotlin.math.max
import kotlin.random.Random

class MainActivity : Activity() {
    private var round=1; private var bananas=100; private var influence=0; private var popularity=50
    private var violence=0; private var votes=0; private var shares=0; private var rivals=7; private var gameOver=false
    private lateinit var topStats: LinearLayout; private lateinit var eventBox: LinearLayout; private lateinit var cards: LinearLayout; private lateinit var footer: TextView

    data class Effect(val bananas:Int=0,val influence:Int=0,val popularity:Int=0,val violence:Int=0,val votes:Int=0,val shares:Int=0,val rivals:Int=0)
    data class Choice(val icon:String,val title:String,val detail:String,val effect:Effect)
    data class Event(val icon:String,val title:String,val text:String,val choices:List<Choice>)

    private val events=listOf(
        Event("🏦","Bananenbank bietet Kredit","Ein Banker-Affe wedelt mit einem verdächtigen Vertrag.",listOf(
            Choice("💰","Kredit nehmen","🍌 +90   ❤️ -5",Effect(bananas=90,popularity=-5)),Choice("✋","Bank ablehnen","❤️ +6",Effect(popularity=6)),Choice("😈","Bank kontrollieren","🍌 -40   📣 +14",Effect(bananas=-40,influence=14)))),
        Event("🌧️","Bananenernte fällt aus","Die Plantagen sind leer. Wer bezahlt die Krise?",listOf(
            Choice("🤲","Vorräte verteilen","🍌 -35   ❤️ +10",Effect(bananas=-35,popularity=10)),Choice("💸","Preise erhöhen","🍌 +25   ❤️ -12",Effect(bananas=25,popularity=-12)),Choice("👉","Andere beschuldigen","📣 +6   👊 +6",Effect(influence=6,violence=6,popularity=-4)))),
        Event("📈","Bananenaktien explodieren","Der Bananenfonds geht komplett zum Mond!",listOf(
            Choice("🤑","Gewinne mitnehmen","🍌 +200 / Aktie",Effect()),Choice("🚀","Mehr kaufen","🍌 -80   📈 +2",Effect(bananas=-80,shares=2)),Choice("📢","Kleinanleger hypen","📣 +10   ❤️ -8",Effect(influence=10,popularity=-8)))),
        Event("🗳️","Wahlkampf!","Die Republik will wissen, wer der Boss-Affe wird.",listOf(
            Choice("🎤","Ehrliche Rede","❤️ +12   🗳️ +8",Effect(popularity=12,votes=8)),Choice("📜","Plakate überall","🍌 -25   🗳️ +12",Effect(bananas=-25,votes=12)),Choice("💵","Stimmen kaufen","🍌 -60   🗳️ +18",Effect(bananas=-60,votes=18,popularity=-5)))),
        Event("🐒","Rivale wird mächtig","Ein anderer Affe sammelt gefährlich viele Anhänger.",listOf(
            Choice("🤝","Debatte fordern","📣 +8   ❤️ +4",Effect(influence=8,popularity=4)),Choice("💼","Bestechen","🍌 -75   🐒 -1",Effect(bananas=-75,rivals=-1)),Choice("💥","Verschwinden lassen","👊 +18   🐒 -1",Effect(violence=18,rivals=-1,popularity=-12)))),
        Event("✊","Arbeitsaffen streiken","Die Arbeiter wollen ihren Anteil vom Bananenkuchen.",listOf(
            Choice("❤️","Löhne erhöhen","🍌 -30   ❤️ +14",Effect(bananas=-30,popularity=14)),Choice("🙈","Ignorieren","🍌 +10   ❤️ -10",Effect(bananas=10,popularity=-10)),Choice("👊","Streik brechen","👊 +12   🍌 +15",Effect(violence=12,bananas=15,popularity=-15))))
    )

    override fun onCreate(b:Bundle?){super.onCreate(b); buildUi(); renderRound()}
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun panel(color:Int,r:Int=18)=GradientDrawable().apply{setColor(color);cornerRadius=dp(r).toFloat();setStroke(dp(2),Color.argb(90,30,20,10))}
    private fun text(s:String,size:Float,bold:Boolean=false)=TextView(this).apply{text=s;textSize=size;setTextColor(Color.rgb(49,38,27));if(bold)setTypeface(typeface,Typeface.BOLD)}

    private fun buildUi(){
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(12),dp(12),dp(12),dp(18));setBackgroundColor(Color.rgb(218,239,194))}
        val title=text("🐵  APE ECONOMICS  🍌",27f,true).apply{gravity=Gravity.CENTER;background=panel(Color.rgb(126,82,42));setTextColor(Color.WHITE);setPadding(8,12,8,12)}
        root.addView(title,LinearLayout.LayoutParams(-1,-2))
        topStats=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(8),dp(8),dp(8),dp(8))};root.addView(topStats)
        eventBox=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(dp(14),dp(14),dp(14),dp(14));background=panel(Color.rgb(255,235,171))};root.addView(eventBox,LinearLayout.LayoutParams(-1,-2).apply{setMargins(0,4,0,10)})
        cards=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER};root.addView(cards)
        footer=text("🐒  Manchmal ist das kleinste Übel der größte Gewinn.",14f).apply{gravity=Gravity.CENTER;setPadding(6,16,6,8)};root.addView(footer)
        val scroll=ScrollView(this).apply{isFillViewport=true;addView(root)};setContentView(scroll)
    }

    private fun renderRound(){
        if(gameOver)return
        topStats.removeAllViews()
        val row1=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER}
        row1.addView(stat("🍌","$bananas","+30/R"),LinearLayout.LayoutParams(0,dp(70),1f).apply{setMargins(2,2,4,2)})
        row1.addView(stat("📈","$shares","Fonds"),LinearLayout.LayoutParams(0,dp(70),1f).apply{setMargins(4,2,2,2)})
        topStats.addView(row1)
        val status=text("🐵 Runde $round/35     👷👷👷  3/10     🐒 Rivalen $rivals",16f,true).apply{gravity=Gravity.CENTER;setPadding(4,8,4,8)};topStats.addView(status)
        val row2=text("📣 $influence     ❤️ $popularity     🗳️ $votes     👊 $violence",17f,true).apply{gravity=Gravity.CENTER;background=panel(Color.argb(55,20,60,40));setPadding(4,9,4,9)};topStats.addView(row2)
        showEvent(events.random())
    }
    private fun stat(icon:String,value:String,sub:String)=TextView(this).apply{text="$icon  $value\n$sub";textSize=18f;gravity=Gravity.CENTER;setTextColor(Color.WHITE);setTypeface(typeface,Typeface.BOLD);background=panel(Color.rgb(39,70,76))}

    private fun showEvent(e:Event){
        eventBox.removeAllViews();cards.removeAllViews()
        eventBox.addView(text("${e.icon}  ${e.title}",22f,true).apply{gravity=Gravity.CENTER})
        eventBox.addView(text(e.text,14f).apply{gravity=Gravity.CENTER;setPadding(0,5,0,0)})
        val colors=listOf(Color.rgb(197,235,161),Color.rgb(174,214,239),Color.rgb(239,174,157))
        e.choices.forEachIndexed{i,c->
            val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(dp(6),dp(8),dp(6),dp(8));background=panel(colors[i],14)}
            card.addView(text(c.icon,34f).apply{gravity=Gravity.CENTER})
            card.addView(text(c.title,14f,true).apply{gravity=Gravity.CENTER;minLines=2})
            card.addView(text(c.detail,13f,true).apply{gravity=Gravity.CENTER;setPadding(0,5,0,7)})
            val choose=Button(this).apply{text="WÄHLEN";textSize=12f;isAllCaps=false;setTypeface(typeface,Typeface.BOLD);setOnClickListener{applyChoice(c);nextRound()}}
            card.addView(choose,LinearLayout.LayoutParams(-1,dp(48)))
            cards.addView(card,LinearLayout.LayoutParams(0,dp(210),1f).apply{setMargins(dp(3),0,dp(3),0)})
        }
    }

    private fun applyChoice(c:Choice){var b=c.effect.bananas;if(c.title=="Gewinne mitnehmen"){b=shares*200;shares=0};bananas+=b;influence+=c.effect.influence;popularity+=c.effect.popularity;violence+=c.effect.violence;votes+=c.effect.votes;shares+=c.effect.shares;rivals+=c.effect.rivals;footer.text="🐒  Gewählt: ${c.title}";clamp()}
    private fun nextRound(){bananas+=30;if(shares>0)bananas+=shares*Random.nextInt(-15,26);round++;clamp();if(!checkEnd())renderRound()}
    private fun clamp(){bananas=max(0,bananas);popularity=popularity.coerceIn(0,100);influence=influence.coerceIn(0,100);violence=violence.coerceIn(0,100);votes=votes.coerceIn(0,100);shares=max(0,shares);rivals=rivals.coerceIn(0,7)}
    private fun checkEnd():Boolean{val msg=when{rivals<=0&&violence>=50->"👊 GEWALTSIEG!\nDer Dschungel gehört dir.";votes>=60&&popularity>=55->"🗳️ DEMOKRATIESIEG!\nDie Affen wählen dich.";influence>=70&&bananas>=350->"💰 KORRUPTIONSSIEG!\nAlles hat seinen Bananenpreis.";popularity<=0->"💀 VERLOREN\nDer Dschungel jagt dich davon.";round>35->"⏰ VERLOREN\nEin anderer Affe übernimmt.";else->null};if(msg==null)return false;gameOver=true;eventBox.removeAllViews();cards.removeAllViews();eventBox.addView(text(msg,25f,true).apply{gravity=Gravity.CENTER});val b=Button(this).apply{text="🔁 NOCHMAL";setOnClickListener{reset()}};eventBox.addView(b);return true}
    private fun reset(){round=1;bananas=100;influence=0;popularity=50;violence=0;votes=0;shares=0;rivals=7;gameOver=false;footer.text="🐒  Manchmal ist das kleinste Übel der größte Gewinn.";renderRound()}
}
