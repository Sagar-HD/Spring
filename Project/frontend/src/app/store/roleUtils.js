export function normalizeRoles(roles) {
  const values = Array.isArray(roles) ? roles : roles ? [roles] : [];
  return values
    .map((role) => String(role).toUpperCase().replace(/^ROLE_/, ""))
    .filter(Boolean);
}

export function getPrimaryRole(user) {
  const roles = normalizeRoles(user?.roles);
  if (roles.includes("ADMIN")) return "ADMIN";
  if (roles.includes("LIBRARIAN")) return "LIBRARIAN";
  if (roles.includes("USER")) return "USER";
  return null;
}

export function getDashboardPath(user) {
  const paths = {
    ADMIN: "/admin/home",
    LIBRARIAN: "/librarian/home",
    USER: "/home",
  };
  return paths[getPrimaryRole(user)] || "/login";
}
