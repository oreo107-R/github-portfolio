
/*
    Project: TechAssist AI
    Developer: Orion Medrano
    Course: COSC 4272 Senior Seminar

    Description:
    Provides chatbot functionality and rule-based
    technical troubleshooting responses.
*/

// HTML elements
const chatBox = document.getElementById("chat-box");
const userInput = document.getElementById("user-input");
const sendButton = document.getElementById("send-button");

let responseTimer;

// Troubleshooting knowledge base
const knowledgeBase = [

    {
        keywords: ["wifi", "wi-fi", "internet", "network",
                   "router", "connection"],

        response: `Network Troubleshooting

1. Verify that Wi-Fi is enabled.
2. Check if other devices have internet access.
3. Restart your modem and router.
4. Forget the network and reconnect.
5. Run your operating system's network troubleshooter.

Are other devices experiencing the same issue?`
    },

    {
        keywords: ["slow", "performance", "lag", "freezing"],

        response: `Computer Performance Troubleshooting

1. Restart your computer.
2. Open Task Manager using Ctrl + Shift + Esc.
3. Check CPU, memory, and disk usage.
4. Close unnecessary applications.
5. Disable unnecessary startup programs.
6. Check available storage space.

Which resource shows the highest usage?`
    },

    {
        keywords: ["windows", "update", "crash",
                   "blue screen", "bsod"],

        response: `Windows Troubleshooting

1. Save your work and restart the computer.
2. Check for available Windows updates.
3. Review recently installed applications.
4. Check Device Manager for driver errors.
5. Record any error messages or error codes.

What version of Windows are you using?`
    },

    {
        keywords: ["hardware", "monitor", "screen",
                   "keyboard", "mouse", "usb"],

        response: `Hardware Troubleshooting

1. Verify all physical connections.
2. Check that the device has power.
3. Disconnect and reconnect the device.
4. Test another cable or USB port.
5. Check Device Manager for errors.
6. Test the device on another computer.

Which hardware device is experiencing the issue?`
    },

    {
        keywords: ["cybersecurity", "virus", "malware",
                   "hacked", "phishing", "security"],

        response: `Cybersecurity Assistance

1. Avoid opening suspicious links or attachments.
2. Disconnect from the network if an active
   malware infection is suspected.
3. Run a trusted antivirus scan.
4. Change compromised passwords from a clean device.
5. Enable multi-factor authentication.
6. Install available security updates.

What suspicious activity have you noticed?`
    },

    {
        keywords: ["password", "login", "account",
                   "sign in", "locked"],

        response: `Account Troubleshooting

1. Verify your username or email address.
2. Check whether Caps Lock is enabled.
3. Use the official password recovery option.
4. Check your authentication methods.
5. Contact the official service provider if needed.

Never share your password or verification codes.

Which service are you trying to access?`
    },

    {
        keywords: ["printer", "printing", "print"],

        response: `Printer Troubleshooting

1. Verify the printer is powered on.
2. Check the USB or network connection.
3. Check paper and ink levels.
4. Review the print queue.
5. Restart the printer.
6. Check for updated printer drivers.

Is your printer connected through USB or Wi-Fi?`
    },

    {
        keywords: ["software", "application", "program", "app"],

        response: `Software Troubleshooting

1. Close and reopen the application.
2. Restart your computer.
3. Check for application updates.
4. Verify system requirements.
5. Record any error messages.
6. Back up application data before reinstalling.

Which application is causing the problem?`
    }

];

// Add a message to the conversation
function addMessage(message, sender) {

    const messageElement = document.createElement("div");

    messageElement.classList.add("message", sender);

    messageElement.textContent = message;

    chatBox.appendChild(messageElement);

    // Scroll to the newest message
    chatBox.scrollTop = chatBox.scrollHeight;
}


// Find the most relevant troubleshooting response
function getResponse(message) {

    const input = message.toLowerCase();

    let bestMatch = null;
    let highestScore = 0;

    for (const topic of knowledgeBase) {

        let score = 0;

        for (const keyword of topic.keywords) {

            if (input.includes(keyword)) {
                score++;
            }
        }

        if (score > highestScore) {
            highestScore = score;
            bestMatch = topic;
        }
    }

    if (bestMatch) {
        return bestMatch.response;
    }

    return `I need a little more information to help you.

Please provide:

1. The device you are using.
2. Your operating system.
3. A description of the problem.
4. Any error messages you received.

Do not include passwords or private information.`;
}


// Send a user message
function sendMessage() {

    const message = userInput.value.trim();

    if (message === "" || sendButton.disabled) {
        return;
    }

    addMessage(message, "user");

    userInput.value = "";

    sendButton.disabled = true;

    // Brief delay before displaying the response
    responseTimer = setTimeout(() => {

        const response = getResponse(message);

        addMessage(response, "assistant");

        sendButton.disabled = false;

        userInput.focus();

    }, 600);
}


// Quick troubleshooting buttons
function quickMessage(message) {

    if (sendButton.disabled) {
        return;
    }

    userInput.value = message;

    sendMessage();
}


// Clear conversation
function clearChat() {

    clearTimeout(responseTimer);

    chatBox.innerHTML = "";

    userInput.value = "";

    sendButton.disabled = false;

    addMessage(
        "Hello! I'm TechAssist AI. Describe your " +
        "technical issue, and I'll help you troubleshoot it.",
        "assistant"
    );

    userInput.focus();
}


// Allow Enter to send a message
userInput.addEventListener("keydown", function(event) {

    if (event.key === "Enter") {

        event.preventDefault();

        sendMessage();
    }
});
