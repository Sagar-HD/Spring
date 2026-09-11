import api from "../app/axios/Axios.js";

export async function register(userData) {

    
        const response = await api.post("/auth/register", userData);
        return response.data;
  
}


export async function login(userData) {


        const response = await api.post("/auth/login", userData);
        console.log(response.data);
        return response.data;
  
}

