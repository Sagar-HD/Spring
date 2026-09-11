 import { useSelector } from "react-redux";
import { Navigate } from "react-router-dom";
import { Outlet } from "react-router-dom";
 const Protected = () => {
  
  const status = useSelector((state) => state.auth.status);

  if (status === "checking") {
    return <p>Checking authentication...</p>;
  }

  if (status === "unauthenticated") {
    return <Navigate to="/login" replace />;
  }

  return <Outlet/>;
};
export default Protected;