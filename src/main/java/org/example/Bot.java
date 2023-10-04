package org.example;


import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class Bot extends TelegramLongPollingBot {
    private static final Map<String, UserData> USER_DATA_CACHE = new ConcurrentHashMap();

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            if (callbackData.equals("button_pressed")) {
                UserData userData = USER_DATA_CACHE.get(update.getCallbackQuery().getFrom().getUserName());
                userData.setTime("Как можно скорее");
                sendResponse(chatId, "Вы указали время подачи - " + "Как можно скорее" + "\n" + "Введите предложенную стоимость в BYN: ");
            } else if (callbackData.equals("button_pressed1")) {
                UserData userData = USER_DATA_CACHE.get(update.getCallbackQuery().getFrom().getUserName());
                userData.setIsCash("Наличными");
                sendResponse(chatId, "Вы выбрали оплату - " + "наличными" + "\n" + "Введите ближайшее время подачи(текстом точное время либо нажимите на кнопку) : ");
                sendButton(chatId);
            } else if (callbackData.equals("button_pressed2")) {
                UserData userData = USER_DATA_CACHE.get(update.getCallbackQuery().getFrom().getUserName());
                userData.setIsCash("Безналичный расчет");
                sendResponse(chatId, "Вы выбрали оплату - " + "Безналичный расчет" + "\n" + "Введите ближайшее время подачи(текстом точное время либо нажимите на кнопку) : ");
                sendButton(chatId);
            }
        }
        CompletableFuture.runAsync(() -> handleUpdate(update));
    }

    private void sendResponse(long chatId, String responseText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(responseText);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleUpdate(Update update) {
        String messageFrom = update.getMessage().getFrom().getUserName();
        Long chatId = update.getMessage().getChatId();
        if(update.getMessage().getChatId() != -1001940933894L){
            if (update.getMessage().getText().equals("/start")) {
                USER_DATA_CACHE.remove(messageFrom);
                UserData userData = new UserData().builder().chatId(chatId).build();
                USER_DATA_CACHE.put(messageFrom, userData);
                sendMessage(update.getMessage().getChatId(), "Привет, сейчас я помогу тебе с заказом такси!" + "\n" + "Введите место, откуда забрать: ");
            } else {
                UserData userData = USER_DATA_CACHE.get(messageFrom);
                String message = update.getMessage().getText();
                if (userData.getDestinationFrom() == null) {
                    userData.setDestinationFrom(message);
                    sendMessage(update.getMessage().getChatId(), "Ваше место отправки - " + message + "\n" + "Введите конечный адрес: ");
                } else if (userData.getDestinationFrom() != null && userData.getDestinationTo() == null) {
                    userData.setDestinationTo(message);
                    sendMessage(update.getMessage().getChatId(), "Ваша конечная точка - " + message + "\n" + "Введите оплата наличными или безнал: ");
                    sendButtonCash(chatId);
                } else if (userData.getDestinationFrom() != null && userData.getDestinationTo() != null && userData.getIsCash() == null) {
                    if(userData.getIsCash()!=null){
                        sendMessage(update.getMessage().getChatId(), "Вы выбрали оплату - " + message + "\n" + "Введите ближайшее время подачи(текстом точное время либо нажимите на кнопку) : ");
                        sendButton(chatId);
                    }
                } else if (userData.getDestinationFrom() != null && userData.getDestinationTo() != null && userData.getIsCash() != null && userData.getTime() == null) {
                    userData.setTime(message);
                    sendMessage(update.getMessage().getChatId(), "Вы указали время подачи - " + message + "\n" + "Введите предложенную стоимость в BYN: ");
                } else if (userData.getDestinationFrom() != null && userData.getDestinationTo() != null && userData.getIsCash() != null && userData.getTime() != null && userData.getPrice() == null) {
                    userData.setPrice(message);

                    sendMessage(update.getMessage().getChatId(), "Вы указали предложенную стоимость в BYN - " + message + "\n" + "Ваш итоговый заказ: " + "\n" +
                            "Клиент: @" + messageFrom + "\n" +
                            "Место откуда: " + userData.getDestinationFrom() + "\n" +
                            "Конечная точка: " + userData.getDestinationTo() + "\n" +
                            "Оплата: " + userData.getIsCash() + "\n" +
                            "Время подачи: " + userData.getTime() + "\n" +
                            "Стоимость в BYN: " + userData.getPrice() + "\n");


                    sendMessage(-1001940933894L,
                            "❗\uFE0FВНИМАНИЕ, ЗАКАЗ❗\uFE0F" + "\n" +  "\n" +
                            "\uD83E\uDDCDКлиент: @" + messageFrom + "\n" +
                            "\uD83D\uDCCDМесто откуда: " + userData.getDestinationFrom() + "\n" +
                            "\uD83D\uDCCDКонечная точка: " + userData.getDestinationTo() + "\n" +
                            "\uD83D\uDCB5Оплата: " + userData.getIsCash() + "\n" +
                            "\uD83D\uDD54Время подачи: " + userData.getTime() + "\n" +
                            "\uD83D\uDCB8Стоимость в BYN: " + userData.getPrice() + "\n" + "\n" +
                            "❗\uFE0FВодители, предложения по поездке и минимальную стоимость, за которую Вы готовы совершить заказ, отправляйте в комментарии, после чего клиент с Вами свяжется в личных сообщениях.");
                    USER_DATA_CACHE.remove(messageFrom);
                }
            }
        }
    }

    private void sendButton(long chatId) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Как можно скорее");
        inlineKeyboardButton.setCallbackData("button_pressed");
        rowInline.add(inlineKeyboardButton);
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Как можно скорее:");
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendButtonCash(long chatId) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Наличными");
        inlineKeyboardButton1.setCallbackData("button_pressed1");

        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();  // Создайте новый объект для второй кнопки
        inlineKeyboardButton2.setText("Безналичный расчет");
        inlineKeyboardButton2.setCallbackData("button_pressed2");

        rowInline.add(inlineKeyboardButton1);
        rowInline.add(inlineKeyboardButton2);

        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Вариант оплаты :");
        message.setReplyMarkup(markupInline);


        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendResponseButton(long chatId, String responseText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(responseText);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(Long chatId, String text) {

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "YourBotUsername";
    }

    @Override
    public String getBotToken() {
        return "";
    }
}