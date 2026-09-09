import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import { Provider } from 'react-redux';
import store from './app/store/store.js';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import AppLayout from './layout/AppLayout.jsx';
import LoginPage from './pages/LoginPage.jsx';
import Protected from './app/protectedRoute/Protected.jsx';
import Register from './pages/Register.jsx';
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <Provider store={store}>
      <BrowserRouter>
   
      <Routes>
        <Route  element={<AppLayout />}>
          <Route path="/" element={<><Protected><App /></Protected></>} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<Register />} />
        </Route>
      </Routes>
  

  
    </BrowserRouter>
    
    </Provider>
  </StrictMode>,
)
