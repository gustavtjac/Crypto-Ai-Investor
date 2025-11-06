


export function isTokenExpired() {
    const token = localStorage.getItem("token");
    if (!token) return true;

    const payload = JSON.parse(atob(token.split(".")[1]));
    const exp = payload.exp * 1000; // convert seconds → ms
    return Date.now() > exp;
}

export async function authorizedFetch(url, options = {}) {
    const token = localStorage.getItem("token");
    const headers = options.headers || {};
    headers["Authorization"] = `Bearer ${token}`;
    headers["Content-Type"] = "application/json";
    const response = await fetch(url, { ...options, headers });
    return response;
}

export async function getLoggedInUser(){
    const res = await authorizedFetch("http://localhost:8080/api/auth/me");
    return await res.json();
}