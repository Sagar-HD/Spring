import { useState } from "react";
import { useForm} from "react-hook-form";
import {login} from "../service/AuthService";
import { Link, useNavigate } from "react-router-dom";
import { useDispatch } from "react-redux";
import {login as reduxLogin} from "../app/store/authSlice";
import Button from "../components/Button";
import { getDashboardPath } from "../app/store/roleUtils";
const LoginPage = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [error,setError] = useState(null);
  const {
    register,
    handleSubmit,
    watch,
    
    formState: { errors, isSubmitting },
  } = useForm();
  
  const onSubmit = async (data) => {

    try {
    const response = await login(data);
    
       
        dispatch(reduxLogin(response.user));
        localStorage.setItem("token", response.token);
        navigate(getDashboardPath(response.user), { replace: true });
      
  }catch(error)  {
        console.error("Login failed:", error);

        setError(error.response?.data?.message || "Login failed. Please check your credentials.");
      }
  }


  return (
    <>
      <div className="w-full max-w-[60%] mx-auto max-h-[80%] mt-10">
        <form onSubmit={handleSubmit(onSubmit)} className="bg-white shadow-md rounded px-8 pt-6 pb-8 mb-4">
          <div className="mb-4">
            <label
              className="block text-gray-700 text-sm font-bold mb-2"
              htmlFor="username"
            >
              Username
            </label>
            <input
               {...register("name", { required: true })}
              className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              id="username"
              type="text"
              placeholder="Username"
            />
            {errors.name && <span className="text-red-500">username field is required</span>}
          </div>
          <div className="mb-6">
            <label
              className="block text-gray-700 text-sm font-bold mb-2"
              htmlFor="password"
            >
              Password
            </label>
            <input
           {...register("password", { required: true })}
              className="shadow appearance-none border border-red-500 rounded w-full py-2 px-3 text-gray-700 mb-3 leading-tight focus:outline-none focus:shadow-outline"
              id="password"
              type="password"
              placeholder="******************"
            />
            
             {errors.password && <span  className="text-red-500">password is required</span>}
          </div>
          <div className="flex items-center justify-between ">
            <button
              className="bg-blue-500 w-[30%] hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
              type="submit"
              disabled={isSubmitting}
            >
              {isSubmitting ? "Signing In..." : "Sign In"}
            </button>
             <Button type="button" className="w-[30%] text-sm" ><Link to="/register">Register</Link></Button>
           
          </div>
          {error && <p className="text-red-500 text-xs italic mt-4">{error}</p>}
        </form>
        <p className="text-center text-gray-500 text-xs">
          &copy;2020 Acme Corp. All rights reserved.
        </p>
      </div>
    </>
  );
};

export default LoginPage;
