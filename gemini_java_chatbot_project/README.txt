GEMINI AI JAVA CHATBOT PROJECT

What is included
- Java Swing chatbox UI
- Real Gemini AI replies through the Gemini REST API
- Conversation history in the current session
- Uses GEMINI_API_KEY or GOOGLE_API_KEY from environment variables

How to run on Windows PowerShell
1. Extract this ZIP file
2. Open the src folder in PowerShell
3. Compile:
   javac *.java
4. Set your key:
   $env:GEMINI_API_KEY="your_real_key_here"
5. Run:
   java ChatBotGUI

How to run on Windows Command Prompt
1. Open the src folder
2. Compile:
   javac *.java
3. Set your key:
   set GEMINI_API_KEY=your_real_key_here
4. Run:
   java ChatBotGUI

Files
- ChatBotGUI.java
- GeminiService.java
- JsonUtils.java
- Message.java

Notes
- Internet connection is required
- Free-tier availability and limits depend on your Google account and region
- If you get quota/rate errors, check your Google AI Studio project and limits
