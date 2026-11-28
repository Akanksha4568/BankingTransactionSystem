import React from "react";
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Landmark } from 'lucide-react';
import { registerRequest } from '../api/auth';
import { getErrorMessage } from '../api/client';

export default function Register() {
  const [form,setForm] = useState({username:'',email:'',password:'',fullName:'',phone:'',address:''});
  const [error,setError] = useState(''); const [success,setSuccess] = useState(''); const [busy,setBusy] = useState(false);
  const navigate = useNavigate();
  const update = e => setForm({...form,[e.target.name]:e.target.value});
const submit = async e => {
  e.preventDefault();
  setError('');
  setSuccess('');
  setBusy(true);

  try {
    const data = await registerRequest(form);

    setSuccess(data?.message || 'Registration successful! Redirecting to sign in...');

    setTimeout(() => {
      navigate('/login', {
        state: { message: 'Registration successful! Please sign in.' }
      });
    }, 1500);

  } catch (err) {
    setError(getErrorMessage(err));
  } finally {
    setBusy(false);
  }
};
  return <div className="register-page"><div className="register-card"><div className="register-brand"><Landmark/> NORTHSTAR BANK</div><span className="eyebrow">GET STARTED</span><h1>Create your account</h1><p className="muted">A few details to set up your banking access.</p><form onSubmit={submit} className="register-grid">{[['fullName','Full name','text'],['username','Username','text'],['email','Email address','email'],['phone','Phone number','tel'],['password','Password','password'],['address','Address','text']].map(([name,label,type])=><label key={name} className={name==='address'?'wide':''}>{label}<input name={name} type={type} value={form[name]} onChange={update} required/></label>)}{error&&<div className="notice error wide">{error}</div>}{success&&<div className="notice wide">{success}</div>}<button className="primary-button full wide" disabled={busy}>{busy?'Creating account…':'Create account'}</button></form><p className="auth-switch">Already registered? <Link to="/login">Sign in</Link></p></div></div>;
}