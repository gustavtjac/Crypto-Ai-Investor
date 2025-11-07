import {isTokenExpired} from "./securityMethods.js";
import {loadLandingPage} from "./landingpage.js";

const app = document.getElementById("app")

export async function loadDashBoard(){

    app.innerHTML= "";
    if (isTokenExpired()){
        return loadLandingPage("Din token er udløbet")
    }
//container der holder hele siden
    const dashboardContainer = document.createElement("div")
    //laver header
    const dashBoardHeaderDiv = document.createElement("div")
    const backBtnImage = document.createElement("img")
    backBtnImage.src = "pictures/backbutton.png"
    backBtnImage.addEventListener("click",async function(){
        return loadLandingPage();
    })
    dashBoardHeaderDiv.appendChild(backBtnImage)
    dashboardContainer.appendChild(dashBoardHeaderDiv)


    //se alle crypto priser


    app.appendChild(dashboardContainer)



}