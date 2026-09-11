import { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { NavLink, useNavigate } from "react-router-dom";
import {
  logout as reduxLogout,
  selectUserData,
} from "../../app/store/authSlice";
import { navItems } from "./NavItems";
import Button from "../Button";
import { getDashboardPath, getPrimaryRole } from "../../app/store/roleUtils";
function Header() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const userData = useSelector(selectUserData);
  const role = getPrimaryRole(userData);
  const items = navItems[role] ?? [];
  function logout() {
    localStorage.removeItem("token");
    dispatch(reduxLogout());
    setIsMenuOpen(false);
    navigate("/login", { replace: true });
  }
  function closeMenu() {
    setIsMenuOpen(false);
  }
  return (
    <nav
      className=" flex py-2 px-4 md:px-8 bg-white border-b border-slate-300 dark:border-neutral-700 dark:bg-neutral-900 min-h-[68px] relative z-20 "
      aria-label="Main navigation"
    >
      {" "}
      <div className=" max-w-7xl mx-auto flex flex-wrap items-center gap-4 w-full ">
        {" "}
       
        <NavLink
          to={getDashboardPath(userData)}
          onClick={closeMenu}
          className=" min-w-9 inline-block focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 rounded "
        >
          
          <span className="sr-only"> Library Management </span>
          <img
            src="https://readymadeui.com/logo-alt.svg"
            alt="Library Management"
            className="h-9 w-auto"
          />
        </NavLink>

        <button
          type="button"
          aria-label={isMenuOpen ? "Close main menu" : "Open main menu"}
          aria-expanded={isMenuOpen}
          aria-controls="collapseMenu"
          onClick={() => setIsMenuOpen((prev) => !prev)}
          className=" ml-auto lg:hidden p-2 rounded cursor-pointer focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 "
        >
        
          {isMenuOpen ? (
            /* CLOSE ICON */ <svg
              xmlns="http://www.w3.org/2000/svg"
              className=" size-5 fill-slate-900 dark:fill-slate-50 "
              viewBox="0 0 329.269 329"
              aria-hidden="true"
            >
              {" "}
              <path d=" M194.8 164.77 323.013 36.555 c8.343-8.34 8.343-21.825 0-30.164 -8.34-8.34-21.825-8.34-30.164 0 L164.633 134.605 36.422 6.391 c-8.344-8.34 -21.824-8.34 -30.164 0 -8.344 8.34 -8.344 21.824 0 30.164 l128.21 128.215 L6.259 292.984 c-8.344 8.34 -8.344 21.825 0 30.164 a21.27 21.27 0 0 0 15.082 6.25 c5.46 0 10.922-2.09 15.082-6.25 l128.21-128.214 128.216 128.214 a21.27 21.27 0 0 0 15.082 6.25 c5.46 0 10.922-2.09 15.082-6.25 8.343-8.34 8.343-21.824 0-30.164z " />{" "}
            </svg>
          ) : (
            /* HAMBURGER ICON */ <svg
              xmlns="http://www.w3.org/2000/svg"
              className=" size-6 fill-slate-900 dark:fill-slate-50 "
              viewBox="0 0 24 24"
              aria-hidden="true"
            >
              {" "}
              <path
                d=" M4 6h16 M4 12h16 M4 18h16 "
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
              />{" "}
            </svg>
          )}{" "}
        </button>{" "}
        {/* ================= NAVIGATION MENU ================= */}{" "}
        <div
          id="collapseMenu"
          className={` ${isMenuOpen ? "block" : "hidden"} lg:block max-lg:bg-white dark:max-lg:bg-neutral-900 max-lg:border-l max-lg:border-slate-300 dark:max-lg:border-neutral-700 max-lg:w-1/2 max-lg:fixed max-lg:top-0 max-lg:right-0 max-lg:h-full max-lg:shadow-md max-lg:overflow-auto max-sm:w-full z-50 outline-none `}
          aria-hidden={!isMenuOpen}
        >
          {" "}
          {/* ================= MOBILE MENU HEADER ================= */}{" "}
          <div className=" py-2 px-4 flex justify-between items-center border-b border-slate-300 sticky top-0 bg-white dark:border-neutral-700 dark:bg-neutral-900 lg:hidden max-lg:min-h-[68px] ">
            {" "}
            <NavLink
              to={getDashboardPath(userData)}
              onClick={closeMenu}
              className=" inline-block focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 rounded "
            >
              {" "}
              <span className="sr-only"> Library Management </span>{" "}
              <img
                src="https://readymadeui.com/logo-alt.svg"
                alt="Library Management"
                className="h-9 w-auto"
              />{" "}
            </NavLink>{" "}
            {/* CLOSE BUTTON */}{" "}
            <button
              type="button"
              onClick={closeMenu}
              aria-label="Close main menu"
              className=" cursor-pointer p-2 rounded focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 "
            >
              {" "}
              <svg
                xmlns="http://www.w3.org/2000/svg"
                className=" size-5 fill-slate-900 dark:fill-slate-50 "
                aria-hidden="true"
                viewBox="0 0 329.269 329"
              >
                {" "}
                <path d=" M194.8 164.77 323.013 36.555 c8.343-8.34 8.343-21.825 0-30.164 -8.34-8.34 -21.825-8.34 -30.164 0 L164.633 134.605 36.422 6.391 c-8.344-8.34 -21.824-8.34 -30.164 0 -8.344 8.34 -8.344 21.824 0 30.164 l128.21 128.215 L6.259 292.984 c-8.344 8.34 -8.344 21.825 0 30.164 a21.27 21.27 0 0 0 15.082 6.25 c5.46 0 10.922-2.09 15.082-6.25 l128.21-128.214 128.216 128.214 a21.27 21.27 0 0 0 15.082 6.25 c5.46 0 10.922-2.09 15.082-6.25 8.343-8.34 8.343-21.824 0-30.164z " />{" "}
              </svg>{" "}
            </button>{" "}
          </div>{" "}
          {/* ================= NAVIGATION LINKS ================= */}{" "}
          <ul className=" flex flex-col gap-8 font-semibold text-sm text-slate-900 dark:text-slate-50 lg:flex-row max-lg:p-6 lg:ml-12 ">
            {" "}
            {items.map((item) => (
              <li key={item.to}>
                {" "}
                <NavLink
                  to={item.to}
                  onClick={closeMenu}
                  className={({ isActive }) =>
                    ` rounded focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 ${isActive ? "text-blue-700 dark:text-blue-400" : "hover:text-blue-700 dark:hover:text-blue-400"} `
                  }
                >
                  {" "}
                  {item.label}{" "}
                </NavLink>{" "}
              </li>
            ))}{" "}
          </ul>{" "}
        </div>{" "}
        {/* ================= RIGHT SIDE ================= */}{" "}
        <div className=" flex items-center gap-4 ml-auto ">
          {" "}
          {/* USERNAME */}{" "}
          <span className=" hidden md:block text-sm font-medium text-slate-700 dark:text-slate-200 ">
            {" "}
            {userData?.name || userData?.username}{" "}
          </span>{" "}
          {/* LOGOUT */}{" "}
          <Button
            type="button"
            onClick={logout}
            className=" text-slate-900 text-sm font-semibold hover:text-blue-700 dark:text-slate-50 dark:hover:text-blue-400 focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 rounded "
          >
            {" "}
            Logout{" "}
          </Button>{" "}
        </div>{" "}
      </div>{" "}
    </nav>
  );
}
export default Header;
