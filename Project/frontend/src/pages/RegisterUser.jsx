import React, { use, useState } from 'react'
import { Link } from 'react-router-dom'
import {useForm} from 'react-hook-form'
import {register as registerUser} from '../service/AuthService'
import { useNavigate } from 'react-router-dom'
const RegisterUser = () => {

   const{register, handleSubmit,formState: { errors ,isSubmitting}} = useForm()
   const [err,seterr]=useState(null)
    const navigate = useNavigate()
   async function onSubmit(data){
    
      if(data.password !== data["confirm-password"]){
         alert("Passwords do not match");
         return;
      }
      const userData = {
         email: data.email,
         name: data.name,
         password: data.password
      }
      try{
         await registerUser(userData)
         alert("User registered successfully")
         navigate("/login")
      }
      catch(err){
         console.log(err);
         seterr(err);

      }


   }

  return (
    <main className="px-4 md:px-8 min-h-screen flex flex-col items-center justify-center">
   <div className="max-w-md w-full">
      <div
         className="p-6 rounded-lg bg-white border border-slate-300 shadow-xs md:p-6 dark:bg-neutral-800 dark:border-neutral-700">
         <h1 className="text-slate-900 text-center text-2xl font-bold dark:text-slate-50">Register as User</h1>

         <form className="space-y-6 mt-10" onSubmit={handleSubmit(onSubmit)}>
            <div>
               <label for="email"
                  className="mb-2 text-slate-900 font-medium text-sm inline-block dark:text-slate-50">Email</label>
               <input {...register("email", { required: true })} type="email" id="email" name="email" placeholder="example@domain.com" required
                  className="px-3 py-2.5 text-sm text-slate-900 rounded-md bg-white w-full outline-1 -outline-offset-1 outline-slate-300 focus:outline-2 focus:-outline-offset-2 focus:outline-blue-600 dark:text-slate-50 dark:bg-neutral-700 dark:outline-neutral-600" />
            </div>
            <div>
               <label for="name"
                  className="mb-2 text-slate-900 font-medium text-sm inline-block dark:text-slate-50">Name</label>
               <input {...register("name", { required: true })} type="text" id="name" name="name" placeholder="John Doe" required
                  className="px-3 py-2.5 text-sm text-slate-900 rounded-md bg-white w-full outline-1 -outline-offset-1 outline-slate-300 focus:outline-2 focus:-outline-offset-2 focus:outline-blue-600 dark:text-slate-50 dark:bg-neutral-700 dark:outline-neutral-600" />
            </div>
            <div>
               <label for="password"
                  className="mb-2 text-slate-900 font-medium text-sm inline-block dark:text-slate-50">Password</label>
               <input {...register("password", { required: true })} type="password" id="password" name="password" placeholder="••••••••" required
                  className="px-3 py-2.5 text-sm text-slate-900 rounded-md bg-white w-full outline-1 -outline-offset-1 outline-slate-300 focus:outline-2 focus:-outline-offset-2 focus:outline-blue-600 dark:text-slate-50 dark:bg-neutral-700 dark:outline-neutral-600" />
            </div>
            <div>
               <label for="confirm-password"
                  className="mb-2 text-slate-900 font-medium text-sm inline-block dark:text-slate-50">Confirm
                  password</label>
               <input {...register("confirm-password", { required: true })} type="password" id="confirm-password" name="confirm-password" placeholder="••••••••" required
                  className="px-3 py-2.5 text-sm text-slate-900 rounded-md bg-white w-full outline-1 -outline-offset-1 outline-slate-300 focus:outline-2 focus:-outline-offset-2 focus:outline-blue-600 dark:text-slate-50 dark:bg-neutral-700 dark:outline-neutral-600" />
            </div>


            <button type="submit"
               className="w-full py-2 px-3.5 text-sm rounded-md font-semibold cursor-pointer tracking-wide text-white border border-blue-600 bg-blue-600 hover:bg-blue-700 transition-all focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500">
               Create an account</button>
         </form>

         <div className="mt-6 text-slate-900 text-sm text-center dark:text-slate-50">Already have an account? <Link to="/login">
               <span className="text-blue-700 hover:underline ml-1 font-medium dark:text-blue-500 focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 rounded">
               Login here</span></Link>
         </div>
         {err && <span className='text-red-200'>{err.message} </span>}
      </div>
   </div>
</main>
  )
}

export default RegisterUser