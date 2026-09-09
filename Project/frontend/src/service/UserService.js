import api from "../app/axios/Axios.js";
export async function getUserData() {
    return await api.get("/users/me");
}