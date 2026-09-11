import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import App from "./App.jsx";
import { Provider } from "react-redux";
import store from "./app/store/store.js";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import AppLayout from "./layout/AppLayout.jsx";
import LoginPage from "./pages/LoginPage.jsx";
import Protected from "./app/routes/Protected.jsx";
import Public from "./app/routes/Public.jsx";
import StartUp from "./app/StartUp.jsx";
import BorrowedBooks from "./pages/BorrowedBooks.jsx";
import Books from "./pages/Books.jsx";
import RegisterUser from "./pages/RegisterUser.jsx";
import RoleRoute from "./app/routes/RoleRoute.jsx";
import Users from "./pages/Users.jsx";
import AdminDashboard from "./pages/AdminDashboard.jsx";
import LibrarianDashboard from "./pages/LibrarianDashboard.jsx";

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <Provider store={store}>
      <BrowserRouter>
        <StartUp>
          <Routes>
            <Route element={<Public />}>
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterUser />} />
            </Route>
            <Route element={<Protected />}>


              <Route element={<AppLayout />}>

               {/* User specific routes */}
               <Route element={<RoleRoute allowedRoles={["USER"]}/>}>
              <Route path="/borrows" element={<BorrowedBooks />} />
              <Route path="/books" element={<Books />} />
              <Route path="/home" element={<App />} />
               </Route>

               <Route element={<RoleRoute allowedRoles={["ADMIN"]}/>}>
                 <Route path="/admin/home" element={<AdminDashboard />} />
                 <Route path="/admin/books" element={<Books manage />} />
                 <Route path="/admin/borrows" element={<BorrowedBooks manage />} />
                 <Route path="/admin/users" element={<Users />} />
               </Route>
               <Route element={<RoleRoute allowedRoles={["LIBRARIAN"]}/>}>
                 <Route path="/librarian/home" element={<LibrarianDashboard />} />
                 <Route path="/librarian/books" element={<Books manage />} />
                 <Route path="/librarian/borrows" element={<BorrowedBooks manage />} />
               </Route>

              </Route>
            </Route>
          </Routes>
        </StartUp>
      </BrowserRouter>
    </Provider>
  </StrictMode>,
);
