import React,{useState,useMemo} from "react";
import {createRoot} from "react-dom/client";
import "./styles.css";

const uid=()=>Math.random().toString(36).slice(2,9);
const starter=[
 {id:"a1",name:"Person",abstract:true,x:80,y:80,attrs:["- id: int","- name: String"],methods:["+ getName(): String"]},
 {id:"a2",name:"Student",abstract:false,x:450,y:260,attrs:["- rollNo: int"],methods:["+ study(): void"]}
];
const starterRel=[{id:"r1",from:"a2",to:"a1",type:"inheritance",label:"extends"}];

function memberParse(s){
 const x=s.trim(), visibility=x[0]&&"+-#~".includes(x[0])?x[0]:"+";
 const body=x[0]&&"+-#~".includes(x[0])?x.slice(1).trim():x;
 const m=body.match(/^([A-Za-z_]\\w*)\\s*(?:\\((.*?)\\))?\\s*:\\s*(.+)$/);
 return {visibility,name:m?.[1]||body||"member",params:m?.[2]||"",type:m?.[3]||"String"};
}
function javaType(t){return t.trim().replace("Integer","int").replace("Boolean","boolean").replace("Void","void")||"String"}

function App(){
 const [classes,setClasses]=useState(starter);
 const [rels,setRels]=useState(starterRel);
 const [selected,setSelected]=useState("a1");
 const [tab,setTab]=useState("diagram");
 const [relType,setRelType]=useState("inheritance");
 const [relFrom,setRelFrom]=useState(null);
 const [java,setJava]=useState("");

 const c=classes.find(x=>x.id===selected);
 const update=(id,p)=>setClasses(a=>a.map(x=>x.id===id?{...x,...p}:x));
 const addClass=()=>{const id=uid(),n=classes.length+1;setClasses(a=>[...a,{id,name:"Class"+n,abstract:false,x:80+(n%3)*310,y:70+Math.floor(n/3)*250,attrs:[],methods:[]}]);setSelected(id)};
 const del=()=>{if(!c)return;setClasses(a=>a.filter(x=>x.id!==c.id));setRels(a=>a.filter(r=>r.from!==c.id&&r.to!==c.id));setSelected(classes.find(x=>x.id!==c.id)?.id||null)};
 const addAttr=()=>update(c.id,{attrs:[...c.attrs,"- attribute: String"]});
 const addMethod=()=>update(c.id,{methods:[...c.methods,"+ operation(): void"]});
 const removeMember=(kind,i)=>update(c.id,{[kind]:c[kind].filter((_,j)=>j!==i)});
 const addRelation=(from,to)=>{if(from===to)return;setRels(a=>[...a,{id:uid(),from,to,type:relType,label:relType==="inheritance"?"extends":relType}]);setRelFrom(null)};
 const generate=()=>{
   const out=classes.map(x=>{
     const attrs=x.attrs.map(memberParse), ms=x.methods.map(memberParse);
     const parent=rels.find(r=>r.type==="inheritance"&&r.from===x.id);
     const ext=parent?` extends ${classes.find(y=>y.id===parent.to)?.name||""}`:"";
     let s=`public ${x.abstract?"abstract ":""}class ${x.name}${ext} {\\n`;
     attrs.forEach(a=>{if(a.visibility!=="~")s+=`    ${a.visibility==="+"?"public":a.visibility==="#"?"protected":"private"} ${javaType(a.type)} ${a.name};\\n`});
     if(attrs.length&&ms.length)s+="\\n";
     ms.forEach(m=>{const vis=m.visibility==="-"?"private":m.visibility==="#"?"protected":"public"; const abs=x.abstract&&m.name.toLowerCase().includes("abstract"); s+=`    ${vis} ${abs?"abstract ":""}${javaType(m.type)} ${m.name}(${m.params})${abs?";":" {\\n"+(m.type!=="void"?"        return null;\\n":"")+"    }"}\\n`});
     return s+"}";
   }).join("\\n\\n");
   setJava(out);setTab("java");
 };
 const dims=useMemo(()=>({w:Math.max(1000,...classes.map(x=>x.x+300)),h:Math.max(650,...classes.map(x=>x.y+260))}),[classes]);

 const drag=(e,item)=>{
   if(e.button!==0)return;e.stopPropagation();
   const sx=e.clientX,sy=e.clientY,bx=item.x,by=item.y;
   const move=ev=>{const scale=1000/document.querySelector(".diagram")?.getBoundingClientRect().width||1;update(item.id,{x:Math.max(10,bx+(ev.clientX-sx)*scale),y:Math.max(10,by+(ev.clientY-sy)*scale)})};
   const up=()=>{removeEventListener("mousemove",move);removeEventListener("mouseup",up)};
   addEventListener("mousemove",move);addEventListener("mouseup",up);
 };
 return <div className="app">
  <header><div><div className="eyebrow">UML • SOFTWARE DESIGN</div><h1>UML Class Diagram Generator</h1></div><div className="headBtns"><button onClick={()=>setTab("diagram")} className={tab==="diagram"?"on":""}>Diagram</button><button onClick={generate}>Generate Java</button></div></header>
  <div className="layout">
   <aside className="left">
    <h2>Diagram Tools</h2><button className="primary" onClick={addClass}>＋ Add Class</button>
    <button onClick={()=>{setRelFrom(null)}}>↔ Create Relationship</button>
    <label>Relationship</label><select value={relType} onChange={e=>setRelType(e.target.value)}><option value="inheritance">Inheritance / Generalization</option><option value="association">Association</option><option value="aggregation">Aggregation</option><option value="composition">Composition</option><option value="dependency">Dependency</option></select>
    <p className="tip">For a relationship, click the first class and then the second class.</p>
    <hr/><h3>Classes</h3>
    {classes.map(x=><button key={x.id} className={"classBtn "+(x.id===selected?"sel":"")} onClick={()=>{setSelected(x.id);setTab("diagram")}}><span>◆</span>{x.abstract&&"◈ "}{x.name}</button>)}
   </aside>
   <main>
    <div className="bar"><span><b>{tab==="diagram"?"UML Canvas":"Java Source"}</b> <em>{classes.length} classes • {rels.length} relationships</em></span>{c&&tab==="diagram"&&<button className="delete" onClick={del}>Delete Class</button>}</div>
    {tab==="diagram"?<div className="scroll"><svg className="diagram" viewBox={`0 0 ${dims.w} ${dims.h}`}>
      <defs><pattern id="grid" width="25" height="25" patternUnits="userSpaceOnUse"><path d="M25 0H0V25" fill="none" stroke="#cbd5e1" opacity=".35"/></pattern><marker id="arr" markerWidth="12" markerHeight="12" refX="10" refY="5" orient="auto"><path d="M0 0L10 5L0 10L3 5Z" fill="white" stroke="#596579"/></marker></defs><rect width="100%" height="100%" fill="url(#grid)"/>
      {rels.map(r=>{const a=classes.find(x=>x.id===r.from),b=classes.find(x=>x.id===r.to);if(!a||!b)return null;const ax=a.x+130,ay=a.y+70,bx=b.x+130,by=b.y+70;return <g key={r.id}><line x1={ax} y1={ay} x2={bx} y2={by} className={"rel "+r.type} markerEnd={r.type==="inheritance"||r.type==="dependency"?"url(#arr)":""}/><text x={(ax+bx)/2} y={(ay+by)/2-7} className="relText">{r.label}</text></g>})}
      {classes.map(x=>{const h=90+x.attrs.length*26+x.methods.length*26;return <g key={x.id} transform={`translate(${x.x},${x.y})`} onMouseDown={e=>drag(e,x)} onClick={e=>{e.stopPropagation();setSelected(x.id); if(relFrom&&relFrom!==x.id)addRelation(relFrom,x.id);else if(relFrom===null&&window.__relationshipMode)setRelFrom(x.id)}} className={"uml "+(selected===x.id?"selected":"")}>
       <rect width="260" height={h} rx="10"/><rect width="260" height="54" rx="10" className="head"/>
       {x.abstract&&<text x="130" y="19" textAnchor="middle" className="stereo">&lt;&lt;abstract&gt;&gt;</text>}
       <text x="130" y={x.abstract?40:33} textAnchor="middle" className="name">{x.name}</text><line x1="0" y1="54" x2="260" y2="54"/>
       {x.attrs.map((m,i)=><text key={i} x="12" y={76+i*26} className="member">{m}</text>)}<line x1="0" y1={76+x.attrs.length*26-7} x2="260" y2={76+x.attrs.length*26-7}/>
       {x.methods.map((m,i)=><text key={i} x="12" y={95+x.attrs.length*26+i*26} className="member">{m}</text>)}
      </g>})}
    </svg></div>:<div className="code"><div><b>Generated Java</b><button onClick={()=>navigator.clipboard?.writeText(java)}>Copy</button></div><pre>{java||"Click Generate Java."}</pre></div>}
   </main>
   <aside className="right">
    <h2>Properties</h2>{c?<><label>Class Name</label><input value={c.name} onChange={e=>update(c.id,{name:e.target.value})}/>
     <label className="check"><input type="checkbox" checked={c.abstract} onChange={e=>update(c.id,{abstract:e.target.checked})}/> <b>Abstract Class</b></label>
     <div className="sectionTitle">Attributes <button onClick={addAttr}>＋ Add Attribute</button></div>
     {c.attrs.map((m,i)=><div className="memberRow" key={i}><input value={m} onChange={e=>update(c.id,{attrs:c.attrs.map((v,j)=>j===i?e.target.value:v)})}/><button onClick={()=>removeMember("attrs",i)}>×</button></div>)}
     <div className="sectionTitle">Methods <button onClick={addMethod}>＋ Add Method</button></div>
     {c.methods.map((m,i)=><div className="memberRow" key={i}><input value={m} onChange={e=>update(c.id,{methods:c.methods.map((v,j)=>j===i?e.target.value:v)})}/><button onClick={()=>removeMember("methods",i)}>×</button></div>)}
     <div className="quick"><b>Quick add</b><button onClick={addAttr}>＋ Attribute</button><button onClick={addMethod}>＋ Method</button></div>
     <div className="legend"><b>Visibility</b><span>+ Public</span><span>- Private</span><span># Protected</span><span>~ Package</span></div>
    </>:<p>Select a class.</p>}
   </aside>
  </div>
 </div>
}
window.__relationshipMode=true;
createRoot(document.getElementById("root")).render(<App/>);