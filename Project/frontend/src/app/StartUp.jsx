import {useEffect} from "react";
import { useDispatch } from "react-redux";
import { getUserData } from "../service/UserService";
import { login, authenticatedCheckFailed } from "./store/authSlice";

export default function AuthBootstrap({ children }) {
  const dispatch = useDispatch();

  useEffect(() => {
    const token = localStorage.getItem("token");

    if (!token) {
      dispatch(authenticatedCheckFailed());
      return;
    }

    getUserData()
      .then((response) => {
        dispatch(login(response.data));
      })
      .catch(() => {
        localStorage.removeItem("token");
        dispatch(authenticatedCheckFailed());
      });
  }, [dispatch]);

  return children;
}