/* TimeFlow v1.1 – isolated, testable session calculations. */
(function(root,factory){
  var api=factory();
  if(typeof module!=='undefined'&&module.exports)module.exports=api;
  root.TimeFlowMath=api;
})(typeof globalThis!=='undefined'?globalThis:this,function(){
  'use strict';
  function parseDuration(raw){
    var s=String(raw==null?'':raw).trim();
    if(!s)return null;
    var parts=s.split(':');
    if(parts.length===1){
      if(!/^\d+$/.test(s))return null;
      var minutes=Number(s);
      return Number.isSafeInteger(minutes)&&minutes<=999999?minutes*60000:null;
    }
    if(parts.length!==2&&parts.length!==3)return null;
    if(!/^\d+$/.test(parts[0])||!/^\d{1,2}$/.test(parts[1]))return null;
    if(parts.length===3&&!/^\d{1,2}$/.test(parts[2]))return null;
    var h=Number(parts[0]),m=Number(parts[1]),sec=parts.length===3?Number(parts[2]):0;
    if(h>999999||m>59||sec>59)return null;
    return ((h*60+m)*60+sec)*1000;
  }
  function formatDuration(ms){
    var seconds=Math.floor(Math.max(0,ms)/1000);
    var h=Math.floor(seconds/3600),m=Math.floor(seconds%3600/60),s=seconds%60;
    return String(h).padStart(2,'0')+':'+String(m).padStart(2,'0')+':'+String(s).padStart(2,'0');
  }
  function workMs(session,now){
    if(typeof session.workMs==='number')return Math.max(0,session.workMs);
    return (session.segments||[]).reduce(function(n,seg){
      return n+Math.max(0,(seg.end||now||Date.now())-seg.start);
    },0);
  }
  function pauseMs(session,now){
    if(typeof session.pauseMs==='number')return Math.max(0,session.pauseMs);
    var end=session.end||now||Date.now();
    return Math.max(0,end-session.start-workMs(session,now));
  }
  function initial(session,now){
    var n=now||Date.now();
    var start=session?session.start:n-3600000,end=session?(session.end||n):n;
    return {start:start,end:end,workMs:session?workMs(session,n):3600000,
      pauseMs:session?pauseMs(session,n):0};
  }
  function update(old,field,value){
    var next={start:old.start,end:old.end,workMs:old.workMs,pauseMs:old.pauseMs};
    if(['start','end','workMs','pauseMs'].indexOf(field)<0)return {error:'Unbekanntes Eingabefeld.',field:field};
    if(!Number.isFinite(value)||value<0)return {error:'Bitte gib einen gültigen Wert ein.',field:field};
    next[field]=value;
    if(field==='start'||field==='workMs'){
      next.end=next.start+next.workMs+next.pauseMs;
    } else if(field==='end'||field==='pauseMs'){
      next.workMs=next.end-next.start-next.pauseMs;
    }
    if(!Number.isFinite(next.end)||next.end<=next.start) return {error:'Die Endzeit muss nach der Startzeit liegen.',field:field};
    if(next.pauseMs>next.end-next.start) return {error:'Die Pausenzeit darf nicht länger als die gesamte Session sein.',field:field};
    if(next.workMs<0)return {error:'Die Arbeitszeit kann nicht negativ sein.',field:field};
    if(next.workMs>next.end-next.start)return {error:'Die Arbeitszeit ist länger als die Session.',field:field};
    if(next.end>8640000000000000)return {error:'Die berechnete Endzeit liegt außerhalb des gültigen Datumsbereichs.',field:field};
    return {value:next};
  }
  function localDate(ts){
    var d=new Date(ts);
    return d.getFullYear()+'-'+String(d.getMonth()+1).padStart(2,'0')+'-'+String(d.getDate()).padStart(2,'0');
  }
  function dayBreakdown(session,now){
    var n=now||Date.now(),days={};
    function add(k,ms){days[k]=(days[k]||0)+ms;}
    function portions(start,end,work){
      if(end<=start||work<=0)return;
      var a=start,remaining=work,span=end-start;
      while(a<end){
        var midnight=new Date(a);midnight.setHours(24,0,0,0);
        var stop=Math.min(midnight.getTime(),end);
        if(stop<=a)break;
        var piece=stop===end?remaining:Math.floor(work*(stop-a)/span);
        add(localDate(a),piece);remaining-=piece;a=stop;
      }
    }
    if(typeof session.workMs==='number'&&session.end){
      // Once a session's aggregate times are edited, precise pause locations are unknown.
      // Allocate working time proportionally by elapsed calendar-day intervals.
      portions(session.start,session.end,Math.max(0,session.workMs));
    }else{
      (session.segments||[]).forEach(function(seg){
        portions(seg.start,seg.end||n,Math.max(0,(seg.end||n)-seg.start));
      });
    }
    return days;
  }
  return {parseDuration:parseDuration,formatDuration:formatDuration,workMs:workMs,
    pauseMs:pauseMs,initial:initial,update:update,dayBreakdown:dayBreakdown};
});
