
/*
    Project: TechAssist AI
    Developer: Orion Medrano
    Course: COSC 4272 Senior Seminar

    Description:
    An AI-assisted, rule-based IT troubleshooting
    application with contextual conversation handling,
    keyword recognition, and guided troubleshooting.
*/

// HTML elements
const chatBox = document.getElementById("chat-box");
const userInput = document.getElementById("user-input");
const sendButton = document.getElementById("send-button");

// Conversation state
let currentTopic = null;
let conversationHistory = [];
let responseTimer = null;

// Troubleshooting knowledge base
const knowledgeBase = [

    {
        name: "Network",
        keywords: [
            "wifi", "wi-fi", "internet", "network",
            "router", "connection", "ethernet"
        ],

        question:
            "Can other devices connect to the internet?",

        yes: `The issue may be isolated to your device.

1. Turn Wi-Fi off and back on.
2. Forget the network and reconnect.
3. Restart your device.
4. Check your network adapter settings.
5. Run the network troubleshooter.

Did these steps resolve your issue?`,

        no: `The issue may involve your router or internet provider.

1. Check your modem and router lights.
2. Verify all network cables are connected.
3. Restart your modem and router.
4. Check for an internet service outage.
5. Contact your internet provider if necessary.

Did these steps resolve your issue?`
    },

    {
        name: "Performance",
        keywords: [
            "slow", "performance", "lag",
            "freezing", "freeze", "sluggish"
        ],

        question:
            "Is your entire computer running slowly?",

        yes: `Let's troubleshoot system performance.

1. Save your work and restart your computer.
2. Open Task Manager with Ctrl + Shift + Esc.
3. Check CPU, memory, and disk usage.
4. Close unnecessary applications.
5. Check available storage space.
6. Review startup applications.

Did these steps resolve your issue?`,

        no: `The problem may involve a specific application.

1. Close and reopen the affected application.
2. Check for application updates.
3. Review its system requirements.
4. Check its resource usage in Task Manager.
5. Back up application data before reinstalling.

Did these steps resolve your issue?`
    },

    {
        name: "Windows",
        keywords: [
            "windows", "update", "crash",
            "blue screen", "bsod", "driver"
        ],

        question:
            "Can you still access your Windows desktop?",

        yes: `Let's troubleshoot Windows.

1. Save your work and restart your computer.
2. Check Windows Update.
3. Review recently installed software.
4. Check Device Manager for driver errors.
5. Record any error codes.

Did these steps resolve your issue?`,

        no: `Windows may be experiencing a startup problem.

1. Disconnect unnecessary external devices.
2. Check your power and display connections.
3. Attempt Windows Startup Repair if available.
4. Avoid resetting Windows without a backup.
5. Contact technical support if startup fails.

Did these steps resolve your issue?`
    },

    {
        name: "Hardware",
        keywords: [
            "hardware", "monitor", "screen",
            "keyboard", "mouse", "usb", "display"
        ],

        question:
            "Is the affected device receiving power?",

        yes: `Let's check the hardware connection.

1. Disconnect and reconnect the device.
2. Try another compatible cable or port.
3. Check Device Manager.
4. Test the device on another computer.
5. Check for manufacturer driver updates.

Did these steps resolve your issue?`,

        no: `Let's investigate the power connection.

1. Verify the power cable is connected.
2. Test a different electrical outlet.
3. Check the device's power switch.
4. Inspect cables for visible damage.
5. Contact the manufacturer if power is unavailable.

Did these steps resolve your issue?`
    },

    {
        name: "Cybersecurity",
        keywords: [
            "cybersecurity", "virus", "malware",
            "hacked", "phishing", "security",
            "ransomware", "suspicious"
        ],

        question:
            "Do you suspect your device is infected with malware?",

        yes: `Take these security precautions.

1. Disconnect the affected device from the network.
2. Avoid entering passwords on the device.
3. Run a trusted antivirus scan.
4. Change compromised passwords from a clean device.
5. Enable multi-factor authentication.
6. Contact qualified technical support if needed.

Did these steps resolve your issue?`,

        no: `Let's review your account security.

1. Avoid suspicious links and attachments.
2. Review recent account activity.
3. Change potentially compromised passwords.
4. Enable multi-factor authentication.
5. Install available security updates.

Did these steps resolve your issue?`
    },

    {
        name: "Account",
        keywords: [
            "password", "login", "account",
            "sign in", "locked", "authentication"
        ],

        question:
            "Do you have access to your recovery email or phone?",

        yes: `You may be able to recover your account.

1. Visit the official service provider website.
2. Select the password recovery option.
3. Verify your identity.
4. Create a strong, unique password.
5. Enable multi-factor authentication.

Never share verification codes.

Did these steps resolve your issue?`,

        no: `Additional account recovery may be required.

1. Visit the official account recovery page.
2. Check for alternative verification methods.
3. Contact the official service provider.
4. Avoid third-party account recovery services.

Did these steps resolve your issue?`
    },

    {
        name: "Printer",
        keywords: [
            "printer", "printing", "print"
        ],

        question:
            "Is your printer connected through Wi-Fi?",

        yes: `Let's troubleshoot your wireless printer.

1. Verify the printer is connected to your network.
2. Check that both devices use the same network.
3. Restart the printer.
4. Clear pending print jobs.
5. Check for updated printer drivers.

Did these steps resolve your issue?`,

        no: `Let's check your printer connection.

1. Verify the printer has power.
2. Check the USB or Ethernet cable.
3. Try another compatible port.
4. Check paper and ink levels.
5. Review the print queue.

Did these steps resolve your issue?`
    },

    {
        name: "Software",
        keywords: [
            "software", "application", "program", "app"
        ],

        question:
            "Does the application open successfully?",

        yes: `Let's troubleshoot the application.

1. Check for software updates.
2. Review application settings.
3. Check available system resources.
4. Record any error messages.
5. Restart the application.

Did these steps resolve your issue?`,

        no: `Let's investigate the startup problem.

1. Restart your computer.
2. Verify system requirements.
3. Check for available updates.
4. Record any startup errors.
5. Back up application data before reinstalling.

Did these steps resolve your issue?`
    }

];

// Track the current troubleshooting stage
let stage = "initial";

// Add a message to the conversation
function addMessage(message, sender) {

    const messageElement = document.createElement("div");

    messageElement.classList.add("message", sender);
    messageElement.textContent = message;

    chatBox.appendChild(messageElement);

    conversationHistory.push({
        sender: sender,
        message: message,
        timestamp: new Date().toISOString()
    });

    chatBox.scrollTop = chatBox.scrollHeight;
}

// Identify the most relevant technical topic
function identifyTopic(message) {

    const input = message.toLowerCase();

    let bestMatch = null;
    let highestScore = 0;

    for (const topic of knowledgeBase) {

        let score = 0;

        for (const keyword of topic.keywords) {

            const escaped = keyword.replace(
                /[.*+?^${}()|[\]\\]/g, "\\$&"
            );

            const pattern = new RegExp(
                "\\b" + escaped + "\\b", "i"
            );

            if (pattern.test(input)) {
                score++;
            }
        }

        if (score > highestScore) {

            highestScore = score;
            bestMatch = topic;
        }
    }

    return bestMatch;
}

// Recognize affirmative responses
function isYes(message) {

    return /^(yes|yep|yeah|yup|correct|sure|it does|they can|it is|it works|fixed|resolved)[.!]?$/i
        .test(message.trim());
}

// Recognize negative responses
function isNo(message) {

    return /^(no|nope|nah|not yet|it doesn't|it does not|they can't|it isn't|still not working|not fixed)[.!]?$/i
        .test(message.trim());
}

// Generate contextual troubleshooting responses
function getResponse(message) {

    const input = message.toLowerCase().trim();

    // Allow users to start a different issue
    if (/^(restart|start over|new issue|new problem)[.!]?$/.test(input)) {

        currentTopic = null;
        stage = "initial";

        return "Let's start a new troubleshooting session. Describe your technical issue.";
    }

    // Handle basic greetings
    if (/^(hello|hi|hey)[.!]?$/.test(input)) {

        return "Hello! Describe your technical issue, and I'll help you troubleshoot it.";
    }

    // Handle follow-up questions
    if (currentTopic && stage === "diagnosis") {

        if (isYes(message)) {

            stage = "verification";
            return currentTopic.yes;
        }

        if (isNo(message)) {

            stage = "verification";
            return currentTopic.no;
        }
    }

    // Verify whether troubleshooting was successful
    if (currentTopic && stage === "verification") {

        if (isYes(message)) {

            currentTopic = null;
            stage = "initial";

            return `Excellent! I'm glad the issue is resolved.

You can describe another technical problem whenever you're ready.`;
        }

        if (isNo(message)) {

            const topicName = currentTopic.name;

            currentTopic = null;
            stage = "initial";

            return `The ${topicName.toLowerCase()} issue may require advanced troubleshooting.

1. Record any error messages.
2. Document the troubleshooting steps attempted.
3. Back up important data if possible.
4. Contact qualified technical support.

You can also describe another issue to begin a new session.`;
        }
    }

    // Identify a new technical problem
    const topic = identifyTopic(message);

    if (topic) {

        currentTopic = topic;
        stage = "diagnosis";

        return `${topic.name} Troubleshooting

I'll ask a question to help narrow down the cause.

${topic.question}

Please answer Yes or No.`;
    }

    // Request clarification during troubleshooting
    if (currentTopic && stage === "diagnosis") {

        return `To continue troubleshooting, please answer Yes or No.

${currentTopic.question}`;
    }

    if (currentTopic && stage === "verification") {

        return "Did the troubleshooting steps resolve your issue? Please answer Yes or No.";
    }

    // Default response
    return `I need more information to identify your issue.

Please provide:

1. Your device type.
2. Your operating system.
3. A description of the problem.
4. Any error messages.

Do not include passwords or private information.`;
}

// Send a message
function sendMessage() {

    const message = userInput.value.trim();

    if (message === "" || sendButton.disabled) {
        return;
    }

    addMessage(message, "user");

    userInput.value = "";
    sendButton.disabled = true;

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

    currentTopic = null;
    stage = "initial";

    userInput.value = message;

    sendMessage();
}

// Clear the conversation
function clearChat() {

    clearTimeout(responseTimer);

    chatBox.innerHTML = "";

    currentTopic = null;
    conversationHistory = [];
    stage = "initial";

    userInput.value = "";
    sendButton.disabled = false;

    addMessage(
        "Hello! I'm TechAssist AI. Describe your " +
        "technical issue, and I'll help you troubleshoot it.",
        "assistant"
    );

    userInput.focus();
}

// Send messages using the Enter key
userInput.addEventListener("keydown", function(event) {

    if (event.key === "Enter") {

        event.preventDefault();
        sendMessage();
    }
});
