import api from "../app/axios/Axios.js";
export async function getMyBorrowedBooks() {
    return await api.get("/borrow/my");
}