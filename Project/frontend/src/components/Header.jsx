
import {useDispatch} from 'react-redux'

import { logout as reduxLogout } from '../app/store/authSlice';
import { useNavigate } from 'react-router-dom';
function Header() {
    const dispatch = useDispatch();
    const navigate = useNavigate();
        function logout() {
            localStorage.removeItem("token");
            dispatch(reduxLogout());
            navigate("/login");

             
        }
  return (
    <div  className="w-full bg-black text-white p-4 flex justify-between items-center">
        <p> Library Management System</p>
        <ul>
            <li onClick={logout}>Logout</li>
        </ul>
    </div>
  )
}

export default Header