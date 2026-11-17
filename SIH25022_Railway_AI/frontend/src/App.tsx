import { useEffect, useState } from 'react'
import { Activity, AlertTriangle, ArrowRight, BrainCircuit, CheckCircle2, Clock3, Gauge, Play, RefreshCw, TrainFront, XCircle } from 'lucide-react'

const API = 'http://localhost:8080/api'
type Train = {id:string;name:string;type:string;priority:number;speedKmph:number;lengthMeters:number;positionKm:number;destinationKm:number;delayMinutes:number;status:string}
type Recommendation = {id:string;preferredTrainId:string;waitingTrainId:string;reason:string;estimatedDelaySavedMinutes:number;status:string}
type Conflict = {type:string;trainA:string;trainB:string;separationKm:number;severity:string;message:string}
export default function App(){
 const [trains,setTrains]=useState<Train[]>([]), [section,setSection]=useState<any>(null)
 const [recommendations,setRecommendations]=useState<Recommendation[]>([]), [conflicts,setConflicts]=useState<Conflict[]>([])
 const [busy,setBusy]=useState(false), [error,setError]=useState('')
 async function load(){
  try { const [t,s,r,c]=await Promise.all([fetch(API+'/trains'),fetch(API+'/section'),fetch(API+'/recommendations'),fetch(API+'/conflicts')])
   if(!t.ok||!s.ok||!r.ok||!c.ok) throw new Error('Backend API is unavailable')
   setTrains(await t.json());setSection(await s.json());setRecommendations(await r.json());setConflicts(await c.json());setError('')
  } catch(e){setError('Cannot reach backend. Start Spring Boot on port 8080.');}
 }
 useEffect(()=>{load();const timer=setInterval(load,4000);return()=>clearInterval(timer)},[])
 async function action(path:string,body?:any){
  setBusy(true);try{const res=await fetch(API+path,{method:'POST',headers:{'Content-Type':'application/json'},body:body?JSON.stringify(body):undefined});if(!res.ok)throw new Error(await res.text());await load()}catch(e){setError(String(e))}finally{setBusy(false)}
 }
 const active=trains.filter(t=>t.status==='RUNNING').length
 const avgDelay=trains.length?Math.round(trains.reduce((a,t)=>a+t.delayMinutes,0)/trains.length):0
 return <main className="shell">
  <header><div className="brand"><div className="brandIcon"><TrainFront/></div><div><div className="eyebrow">SIH25022 · DECISION SUPPORT SIMULATOR</div><h1>RailAI <span>Control Centre</span></h1></div></div><div className="live"><i/> SIMULATION ENVIRONMENT</div></header>
  {error&&<div className="error"><AlertTriangle size={17}/>{error}</div>}
  <section className="hero"><div><div className="eyebrow light">SECTION OVERVIEW</div><h2>{section?.name||'Navapura – Devgarh Section'}</h2><p>AI-assisted train precedence and traffic flow recommendations</p></div><div className="heroRight"><div className="heroMetric"><strong>{section?.lengthKm??24} <small>KM</small></strong><span>Section length</span></div><div className="heroMetric"><strong>{section?.simulationMinute??0}</strong><span>Simulated minutes</span></div></div></section>
  <section className="metrics">
   <Metric icon={<TrainFront/>} label="Active trains" value={active} tone="blue"/>
   <Metric icon={<AlertTriangle/>} label="Open conflicts" value={conflicts.length} tone="orange"/>
   <Metric icon={<Clock3/>} label="Average delay" value={`${avgDelay} min`} tone="purple"/>
   <Metric icon={<Gauge/>} label="Loop points" value={section?.loopPointsKm?.length??2} tone="green"/>
  </section>
  <div className="sectionHead"><div><div className="eyebrow">LIVE OPERATIONS</div><h2>Section traffic</h2></div><div className="actions"><button className="secondary" onClick={load}><RefreshCw size={15}/> Refresh</button><button disabled={busy} onClick={()=>action('/simulation/tick')}><Play size={15}/> Advance 1 minute</button></div></div>
  <section className="panel">
   <div className="trackHead"><span>SIMULATED TRACK · 0–24 KM</span><span>LOOPS AT 8 KM AND 16 KM</span></div>
   <div className="track"><div className="trackLine"/><div className="loop l1">LOOP 1</div><div className="loop l2">LOOP 2</div>{trains.map(t=><div key={t.id} className="trainMarker" style={{left:`${Math.max(1,Math.min(96,t.positionKm/24*100))}%`}} title={`${t.name}: ${t.positionKm.toFixed(1)} km`}><span className={t.type==='FREIGHT'?'freight':'passenger'}><TrainFront size={15}/></span><small>{t.id}</small></div>)}</div>
   <div className="trackScale"><span>0 km · Navapura</span><span>8 km</span><span>16 km</span><span>24 km · Devgarh</span></div>
  </section>
  <div className="columns">
   <section className="panel"><div className="panelTitle"><div><h3>Train registry</h3><p>Current simulated train state</p></div><span className="count">{trains.length} trains</span></div>
    <div className="tableWrap"><table><thead><tr><th>TRAIN</th><th>POSITION</th><th>SPEED</th><th>DELAY</th><th>PRIORITY</th><th>STATUS</th></tr></thead><tbody>{trains.map(t=><tr key={t.id}><td><b>{t.name}</b><small>{t.id} · {t.type}</small></td><td>{t.positionKm.toFixed(1)} km</td><td>{t.speedKmph} km/h</td><td>{t.delayMinutes} min</td><td><span className="priority">P{t.priority}</span></td><td><span className={'status '+t.status.toLowerCase()}>{t.status}</span></td></tr>)}</tbody></table></div>
   </section>
   <section className="panel"><div className="panelTitle"><div><h3>Conflict monitor</h3><p>Potential opposing movements</p></div><AlertTriangle className="muted"/></div>
    {conflicts.length===0?<div className="empty"><CheckCircle2/><b>No active conflicts detected</b><span>Within the current simplified simulation model.</span></div>:conflicts.map((c,i)=><div className="conflict" key={i}><div className="conflictTop"><b>{c.trainA} ↔ {c.trainB}</b><span className={'severity '+c.severity.toLowerCase()}>{c.severity}</span></div><p>{c.message}</p><small>Separation: {c.separationKm} km</small></div>)}
   </section>
  </div>
  <section className="panel recommendations"><div className="panelTitle"><div><div className="eyebrow">HUMAN-IN-THE-LOOP</div><h3>Scheduling recommendations</h3><p>Heuristic precedence suggestions · Controller approval required</p></div><button disabled={busy} onClick={()=>action('/scheduling/recommend')}><BrainCircuit size={16}/> Generate recommendation</button></div>
   {recommendations.length===0?<div className="empty"><BrainCircuit/><b>No recommendations generated yet</b><span>Generate a precedence suggestion from the current train state.</span></div>:<div className="recGrid">{[...recommendations].reverse().map(r=><article className="rec" key={r.id}><div className="recTop"><span className="recId">{r.id}</span><span className={'status '+(r.status==='PENDING'?'pending':'accepted')}>{r.status.replaceAll('_',' ')}</span></div><div className="precedence"><div><small>RECOMMENDED FIRST</small><b>{r.preferredTrainId}</b></div><ArrowRight/><div><small>WAITING TRAIN</small><b>{r.waitingTrainId}</b></div></div><p>{r.reason}</p><div className="saving"><Activity size={15}/> Estimated delay saving: <b>{r.estimatedDelaySavedMinutes} min</b></div>{r.status==='PENDING'&&<div className="decision"><button onClick={()=>action(`/recommendations/${r.id}/decision`,{decision:'ACCEPT'})}><CheckCircle2 size={15}/> Accept</button><button className="reject" onClick={()=>action(`/recommendations/${r.id}/decision`,{decision:'REJECT'})}><XCircle size={15}/> Reject</button></div>}</article>)}</div>}
  </section>
  <footer>RAILAI · SIH25022 <span>Simulation only — not connected to real railway operations or signalling.</span></footer>
 </main>
}
function Metric({icon,label,value,tone}:{icon:any;label:string;value:any;tone:string}){return <div className="metric"><div className={'metricIcon '+tone}>{icon}</div><div><span>{label}</span><strong>{value}</strong></div></div>}
