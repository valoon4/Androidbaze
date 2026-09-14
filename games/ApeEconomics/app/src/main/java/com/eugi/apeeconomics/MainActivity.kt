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
    private lateinit var topStats:LinearLayout; private lateinit var eventBox:LinearLayout; private lateinit var cards:LinearLayout; private lateinit var footer:TextView

    data class Effect(val bananas:Int=0,val influence:Int=0,val popularity:Int=0,val violence:Int=0,val votes:Int=0,val shares:Int=0,val rivals:Int=0)
    data class Choice(val art:String,val title:String,val detail:String,val effect:Effect)
    data class Event(val title:String,val text:String,val choices:List<Choice>)

    private val events=listOf(
        Event("Bananenbank bietet Kredit","Ein Banker-Affe wedelt mit einem verdächtigen Vertrag.",listOf(
            Choice("banker","Kredit nehmen","🍌 +90   ❤️ -5",Effect(bananas=90,popularity=-5)),
            Choice("politician","Bank ablehnen","❤️ +6",Effect(popularity=6)),
            Choice("soldier","Bank kontrollieren","🍌 -40   📣 +14",Effect(bananas=-40,influence=14)))),
        Event("Bananenernte fällt aus","Die Plantagen sind leer. Wer bezahlt die Krise?",listOf(
            Choice("worker","Vorräte verteilen","🍌 -35   ❤️ +10",Effect(bananas=-35,popularity=10)),
            Choice("banker","Preise erhöhen","🍌 +25   ❤️ -12",Effect(bananas=25,popularity=-12)),
            Choice("propaganda","Andere beschuldigen","📣 +6   👊 +6",Effect(influence=6,violence=6,popularity=-4)))),
        Event("Bananenaktien explodieren","Der Bananenfonds geht komplett zum Mond!",listOf(
            Choice("trader","Gewinne mitnehmen","🍌 +200 / Aktie",Effect()),
            Choice("banker","Mehr kaufen","🍌 -80   📈 +2",Effect(bananas=-80,shares=2)),
            Choice("propaganda","Kleinanleger hypen","📣 +10   ❤️ -8",Effect(influence=10,popularity=-8)))),
        Event("Wahlkampf!","Die Republik will wissen, wer der Boss-Affe wird.",listOf(
            Choice("politician","Ehrliche Rede","❤️ +12   🗳️ +8",Effect(popularity=12,votes=8)),
            Choice("propaganda","Plakate überall","🍌 -25   🗳️ +12",Effect(bananas=-25,votes=12)),
            Choice("banker","Stimmen kaufen","🍌 -60   🗳️ +18",Effect(bananas=-60,votes=18,popularity=-5)))),
        Event("Rivale wird mächtig","Ein anderer Affe sammelt gefährlich viele Anhänger.",listOf(
            Choice("politician","Debatte fordern","📣 +8   ❤️ +4",Effect(influence=8,popularity=4)),
            Choice("banker","Bestechen","🍌 -75   🐒 -1",Effect(bananas=-75,rivals=-1)),
            Choice("soldier","Verschwinden lassen","👊 +18   🐒 -1",Effect(violence=18,rivals=-1,popularity=-12)))),
        Event("Arbeitsaffen streiken","Die Arbeiter wollen ihren Anteil vom Bananenkuchen.",listOf(
            Choice("worker","Löhne erhöhen","🍌 -30   ❤️ +14",Effect(bananas=-30,popularity=14)),
            Choice("rival","Ignorieren","🍌 +10   ❤️ -10",Effect(bananas=10,popularity=-10)),
            Choice("soldier","Streik brechen","👊 +12   🍌 +15",Effect(violence=12,bananas=15,popularity=-15))))
    )

    override fun onCreate(b:Bundle?){super.onCreate(b);buildUi();renderRound()}
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun panel(color:Int,r:Int=18)=GradientDrawable().apply{setColor(color);cornerRadius=dp(r).toFloat();setStroke(dp(2),Color.argb(100,42,28,14))}
    private fun text(s:String,size:Float,bold:Boolean=false)=TextView(this).apply{text=s;textSize=size;setTextColor(Color.rgb(49,38,27));if(bold)setTypeface(typeface,Typeface.BOLD)}
    private fun artRes(name:String)=when(name){
        "banker"->R.drawable.ape_banker
        "worker"->R.drawable.ape_worker
        "trader"->R.drawable.ape_trader
        "politician"->R.drawable.ape_politician
        "soldier"->R.drawable.ape_soldier
        "propaganda"->R.drawable.ape_propaganda
        "rival"->R.drawable.ape_rival
        else->R.drawable.ape_worker
    }
    private fun apeImage(name:String)=ImageView(this).apply{
        setImageResource(artRes(name)); scaleType=ImageView.ScaleType.CENTER_INSIDE; adjustViewBounds=true
    }

    private fun buildUi(){
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(10),dp(8),dp(10),dp(16));setBackgroundColor(Color.rgb(190,225,151))}
        root.addView(ImageView(this).apply{setImageResource(R.drawable.ape_logo);scaleType=ImageView.ScaleType.CENTER_INSIDE;adjustViewBounds=true},LinearLayout.LayoutParams(-1,dp(118)))
        topStats=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(4),0,dp(4),dp(7))};root.addView(topStats)
        eventBox=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(dp(14),dp(12),dp(14),dp(12));background=panel(Color.rgb(255,235,171))};root.addView(eventBox,LinearLayout.LayoutParams(-1,-2).apply{setMargins(0,3,0,10)})
        cards=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER};root.addView(cards)
        footer=text("🐒 Manchmal ist das kleinste Übel der größte Gewinn.",13f).apply{gravity=Gravity.CENTER;setPadding(6,12,6,6)};root.addView(footer)
        setContentView(ScrollView(this).apply{isFillViewport=true;addView(root)})
    }

    private fun renderRound(){if(gameOver)return;topStats.removeAllViews();val row1=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        row1.addView(stat("🍌 $bananas","+30/R"),LinearLayout.LayoutParams(0,dp(63),1f).apply{setMargins(2,2,4,2)})
        row1.addView(stat("📈 $shares","FONDS"),LinearLayout.LayoutParams(0,dp(63),1f).apply{setMargins(4,2,2,2)});topStats.addView(row1)
        topStats.addView(text("Runde $round/35     3/10 Affen     Rivalen $rivals",15f,true).apply{gravity=Gravity.CENTER;setPadding(4,7,4,7)})
        topStats.addView(text("📣 $influence     ❤️ $popularity     🗳️ $votes     👊 $violence",16f,true).apply{gravity=Gravity.CENTER;background=panel(Color.rgb(157,191,143));setPadding(4,8,4,8)});showEvent(events.random())}
    private fun stat(main:String,sub:String)=TextView(this).apply{text="$main\n$sub";textSize=17f;gravity=Gravity.CENTER;setTextColor(Color.WHITE);setTypeface(typeface,Typeface.BOLD);background=panel(Color.rgb(39,70,76))}

    private fun showEvent(e:Event){eventBox.removeAllViews();cards.removeAllViews();val eventRow=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
        eventRow.addView(apeImage(e.choices[0].art),LinearLayout.LayoutParams(dp(90),dp(90)).apply{setMargins(0,0,10,0)})
        eventRow.addView(LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;addView(text(e.title,21f,true));addView(text(e.text,14f).apply{setPadding(0,4,0,0)})},LinearLayout.LayoutParams(0,-2,1f));eventBox.addView(eventRow)
        val colors=listOf(Color.rgb(185,226,145),Color.rgb(159,205,234),Color.rgb(235,156,138));e.choices.forEachIndexed{i,c->val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(dp(5),dp(6),dp(5),dp(7));background=panel(colors[i],14)}
            card.addView(apeImage(c.art),LinearLayout.LayoutParams(-1,dp(108)));card.addView(text(c.title,13f,true).apply{gravity=Gravity.CENTER;minLines=2;setPadding(0,5,0,2)});card.addView(text(c.detail,12f,true).apply{gravity=Gravity.CENTER;setPadding(0,2,0,5)});card.addView(Button(this).apply{text="WÄHLEN";textSize=11f;isAllCaps=false;setTypeface(typeface,Typeface.BOLD);setOnClickListener{applyChoice(c);nextRound()}},LinearLayout.LayoutParams(-1,dp(43)));cards.addView(card,LinearLayout.LayoutParams(0,dp(255),1f).apply{setMargins(dp(3),0,dp(3),0)})}}

    private fun applyChoice(c:Choice){var b=c.effect.bananas;if(c.title=="Gewinne mitnehmen"){b=shares*200;shares=0};bananas+=b;influence+=c.effect.influence;popularity+=c.effect.popularity;violence+=c.effect.violence;votes+=c.effect.votes;shares+=c.effect.shares;rivals+=c.effect.rivals;footer.text="🐒 Gewählt: ${c.title}";clamp()}
    private fun nextRound(){bananas+=30;if(shares>0)bananas+=shares*Random.nextInt(-15,26);round++;clamp();if(!checkEnd())renderRound()}
    private fun clamp(){bananas=max(0,bananas);popularity=popularity.coerceIn(0,100);influence=influence.coerceIn(0,100);violence=violence.coerceIn(0,100);votes=votes.coerceIn(0,100);shares=max(0,shares);rivals=rivals.coerceIn(0,7)}
    private fun checkEnd():Boolean{val msg=when{rivals<=0&&violence>=50->"👊 GEWALTSIEG!\nDer Dschungel gehört dir.";votes>=60&&popularity>=55->"🗳️ DEMOKRATIESIEG!\nDie Affen wählen dich.";influence>=70&&bananas>=350->"💰 KORRUPTIONSSIEG!\nAlles hat seinen Bananenpreis.";popularity<=0->"💀 VERLOREN\nDer Dschungel jagt dich davon.";round>35->"⏰ VERLOREN\nEin anderer Affe übernimmt.";else->null};if(msg==null)return false;gameOver=true;eventBox.removeAllViews();cards.removeAllViews();eventBox.addView(text(msg,25f,true).apply{gravity=Gravity.CENTER});eventBox.addView(Button(this).apply{text="🔁 NOCHMAL";setOnClickListener{reset()}});return true}
    private fun reset(){round=1;bananas=100;influence=0;popularity=50;violence=0;votes=0;shares=0;rivals=7;gameOver=false;footer.text="🐒 Manchmal ist das kleinste Übel der größte Gewinn.";renderRound()}
}
