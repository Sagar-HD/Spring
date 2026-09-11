import axios from "axios";
import api from "../app/axios/Axios.js";
const apiMiddleware = (store) => (next) => (action) => {
    if (action.type === 'api/makeRequest') {
        const { url,onSuccess, onError } = action.payload;
        api.get(url).then((response) => {
           
            store.dispatch({ type: onSuccess, payload: response.data });
        })
        .catch((error) => {
            console.log("apiMiddleware error",error.message);
            store.dispatch({ type: onError, payload: error.message });
        });

    }
    return next(action);
};

export default apiMiddleware;