import type {Book} from '../types/book';
const API='http://localhost:8080/api';
export async function getBooks(category=''){const url=category?`${API}/books?category=${encodeURIComponent(category)}`:`${API}/books`; const r=await fetch(url); if(!r.ok) throw new Error('Unable to load books'); return r.json() as Promise<Book[]>;}
export async function register(data:{fullName:string;email:string;password:string}){const r=await fetch(`${API}/users/register`,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(data)}); const j=await r.json(); if(!r.ok) throw new Error(j.message||'Registration failed'); return j;}
export async function login(data:{email:string;password:string}){const r=await fetch(`${API}/users/login`,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(data)}); const j=await r.json(); if(!r.ok) throw new Error(j.message||'Login failed'); return j;}
