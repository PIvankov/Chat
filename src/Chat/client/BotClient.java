package Chat.client;

import Chat.ConsoleHelper;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class BotClient extends Client {

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }

    public class BotSocketThread extends SocketThread {

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            if (message.indexOf(": ") > 0 && message.indexOf("date_bot_") < 0) {
                String userName = message.substring(0, message.indexOf(": "));
                String userMessage = message.substring(message.indexOf(": ") + 2);

                StringBuilder answer = new StringBuilder("Информация для " + userName + ": ");

                Calendar calendar = new GregorianCalendar();
                Date currentDate = calendar.getTime();

                DateFormat dateFormat = null;

                switch (userMessage) {
                    case "дата" : {
                        dateFormat = new SimpleDateFormat("d.MM.YYYY");
                        answer.append(dateFormat.format(currentDate.getTime()));
                        break;
                    }
                    case "день" : {
                        dateFormat = new SimpleDateFormat("d");
                        answer.append(dateFormat.format(currentDate.getTime()));
                        break;
                    }
                    case "месяц" : {
                        dateFormat = new SimpleDateFormat("MMMM");
                        answer.append(dateFormat.format(currentDate.getTime()));
                        break;
                    }
                    case "год" : {
                        dateFormat = new SimpleDateFormat("YYYY");
                        answer.append(dateFormat.format(currentDate.getTime()));
                        break;
                    }
                    case "время" : {
                        dateFormat = new SimpleDateFormat("H:mm:ss");
                        answer.append(dateFormat.format(currentDate.getTime()));
                        break;
                    }
                    case "час" : {
                        dateFormat = new SimpleDateFormat("H");
                        answer.append(dateFormat.format(currentDate.getTime()));
                        break;
                    }
                    case "минуты" : {
                        dateFormat = new SimpleDateFormat("m");
                        answer.append(dateFormat.format(currentDate.getTime()));
                        break;
                    }
                    case "секунды" : {
                        dateFormat = new SimpleDateFormat("s");
                        answer.append(dateFormat.format(currentDate.getTime()));
                        break;
                    }
                    default: return;
                }

                sendTextMessage(answer.toString());
            }
        }

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        return "date_bot_" + (int) (Math.random() * 100);
    }
}
