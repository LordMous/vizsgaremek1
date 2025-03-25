import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import React from 'react';
import './index.css';
import App from './App.jsx';
import process from 'process'; // Importáld a process-t expliciten

window.global = window;
window.process = process;

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>
);
