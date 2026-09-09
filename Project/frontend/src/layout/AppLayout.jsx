import React from 'react'
import { Outlet } from 'react-router-dom'
import Header from '../components/Header'
const AppLayout = () => {
  return (
    <>
     <Header/>
    <main className="p-4">
      <Outlet />
    </main>
    <footer className="w-full bg-black text-white p-4">Footer</footer>
    
    </>
  )
}

export default AppLayout