import React from 'react'
import { useSelector } from 'react-redux';
import {selectUserData} from '../../app/store/authSlice';
import { Outlet } from 'react-router-dom';
import { normalizeRoles } from '../store/roleUtils';

const RoleRoute = ({allowedRoles}) => {
   
 
    const userData = useSelector(selectUserData);
    const userRoles = normalizeRoles(userData?.roles);
    
    const isAllowed = userRoles.some((role) =>
      allowedRoles.map((allowedRole) => allowedRole.toUpperCase().replace("ROLE_", "")).includes(role)
    );


  if (!isAllowed) {
    return <div>You are not authorized to view this content.{console.log(userData)}</div>;
  }

  return <Outlet/>;
}

export default RoleRoute