const BAR_WIDTH = "690";
const BAR_HEIGHT = "55";
const TRANSITION_MILLIS = 700;
let maxVotes;
let topChallenge = "";

/**
 * Add new option to poll.
 */
function addPollOption() {
  const text = document.getElementById("input-box").value;
  if (text.trim() === "") return;
  document.getElementById("input-box").value = "";
  fetch(`poll?text=${text}`, { method: "POST" }).then(
    setTimeout(getPollOptions, 500)
  );
}

/**
 * Get poll data, which includes each poll option and the * list of options
 * that the current logged in user has voted for, from server and load to DOM.
 */
function getPollOptions() {
  fetch("/poll")
    .then(response => response.json())
    .then(pollData => {
      const optionsContainer = document.getElementById("options-container");
      optionsContainer.innerHTML = "";
      let maxVotes = getMaxVotes(pollData);
      topChallenge = pollData["options"][0]["text"];
      pollData["options"].forEach(option => {
        renderOptionElement(option, maxVotes);
      });
      return pollData["votedOptions"];
    })
    .then(votedOptions => {
      handleCheck(votedOptions);
    })
    .then(updateChallenge);
}

function updateChallenge() {
  const weeklyChallenge = document.getElementById("weekly-challenge");
  weeklyChallenge.innerText = topChallenge;
}

/**
 * Gets the maximum number of votes for the poll. The option with maximum
 * number of votes should be the first option since the options are sorted in
 * order of votes.
 * @param {objct} pollData
 */
function getMaxVotes(pollData) {
  let maxVotes;
  // Gets the list of votes for the first poll option
  let votesArray = pollData["options"][0]["votes"];
  if (votesArray == null) {
    maxVotes = 0;
  } else {
    maxVotes = votesArray.length;
  }
  return maxVotes;
}

/**
 * Handles whether checkbox is checked. Takes in list of ids of
 * checkboxes for which current user has checked and checks if id of
 * current checkbox is in that list.
 * @param {array} votedOptions
 */
function handleCheck(votedOptions) {
  const checkboxes = document.querySelectorAll("input[type=checkbox]");
  for (let i = 0; i < checkboxes.length; i++) {
    let checkbox = checkboxes[i];
    markCheckbox(votedOptions, checkbox);
  }
  return;
}

/**
 * Mark whether checkbox is checked by seeing if current checkbox is in
 * votedOptions, which contains the options for which the currently logged in
 * user has voted.
 * @param {object} votedOptions
 * @param {object} checkbox
 */
function markCheckbox(votedOptions, checkbox) {
  if (votedOptions.find(checkboxId => checkbox.id == checkboxId)) {
    checkbox.checked = true;
  } else {
    checkbox.checked = false;
  }
}

/**
 * Uses template in group HTML file and changes text depending on data
 * being loaded.
 * @param {object} option
 */
function renderOptionElement(option, maxVotes) {
  console.log("rendering");
  let numVotes = 0;
  if (option["votes"]) {
    numVotes = option["votes"].length;
  }
  //Calculate length of inner bar for poll
  let innerBarLength;
  if (maxVotes == 0) {
    innerBarLength = 0;
  } else {
    innerBarLength = (numVotes / maxVotes) * BAR_WIDTH;
  }
  const text = option["text"];

  const optionsContainer = document.getElementById("options-container");
  const optionElement = document.getElementById("option-template");
  const optionElementNode = document.importNode(optionElement.content, true);

  //Set name of challenge
  const challengeName = optionElementNode.querySelector("p");
  const challengeText = document.createTextNode(text);
  challengeName.appendChild(challengeText);
  //Set length of inner bar for poll
  const innerBar = optionElementNode.getElementById("inner-bar");
  innerBar.setAttribute("width", innerBarLength);
  //Set id for checkbox
  const checkbox = optionElementNode.querySelector("input");
  const id = option["id"];
  checkbox.id = id;
  //Set for field for label
  const label = optionElementNode.querySelector("label");
  label.htmlFor = id;
  //Set number of votes per challenge option
  const votesLabel = optionElementNode.getElementById("num-votes");
  const votesString = numVotes === 1 ? "vote" : "votes";
  votesLabel.innerText = `${numVotes} ${votesString}`;
  optionsContainer.appendChild(optionElementNode);
}

/**
 * Handles changing the number of votes when the checkbox is either
 * checked or unchecked.
 * @param {String} id
 * @param {String} checked
 */
function handleCheckboxCount(id, checked) {
  fetch(`update-votes?id=${id}&checked=${checked}`, { method: "POST" }).then(
    setTimeout(getPollOptions, TRANSITION_MILLIS)
  );
}

function getChallenge() {
  console.log("here");
  const weeklyChallenge = document.getElementById("weekly-challenge");
  weeklyChallenge.innerText = topChallenge;
  console.log("top challenge is " + topChallenge);
}

// window.addEventListener("load", function() {
//   this.console.log("after load");
//   const topChallenge = document.getElementsByClassName("option-container");
//   console.log(topChallenge);
//   console.log(topChallenge.length);
// });
