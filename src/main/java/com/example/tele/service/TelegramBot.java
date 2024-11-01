package com.example.tele.service;

import com.example.tele.config.BotConfig;
import com.example.tele.model.OrdersRepository;
import com.example.tele.model.Users;
import com.example.tele.model.UsersRepository;
import com.vdurmont.emoji.EmojiParser;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    private UsersRepository userRepository;
    @Autowired
    private OrdersRepository ordersRepository;
    private static HashMap<String, Users> list_accept = new HashMap<>();
    final BotConfig config;
    static final String HELP_TEXT = "Какая информация нужна\n\n Inf /hlp";
    static final String YES_BUTTON = "YES_BUTTON";
    static final String NO_BUTTON = "NO_BUTTON";
    static final String ERROR_TEXT = "Error occured: ";


    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Начать работу"));
        listOfCommands.add(new BotCommand("/send", "Разослать доставку"));
        listOfCommands.add(new BotCommand("/users", "Список пользователей для добавления"));
        listOfCommands.add(new BotCommand("/list_user", "Список всех пользователей"));
        listOfCommands.add(new BotCommand("/delete", "Удаление пользователя по имени"));
        listOfCommands.add(new BotCommand("/add", "Для добавления пользователя пришлите /add ID или Username"));
//        listOfCommands.add(new BotCommand("/hlp", "help"));
//        listOfCommands.add(new BotCommand("/find", "help"));
//        listOfCommands.add(new BotCommand("/settings", "settings"));
//        listOfCommands.add(new BotCommand("/list_user", "Списко пользователей"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {//Центральный метод, обработка сообщений пользователя
//        Long chatId = update.getMessage().getChatId();
        if (update.hasMessage() && !update.getMessage().hasText()){
            prepareAndMessage(update.getMessage().getChatId(), "Необходимо вводить текст");
        }
//        Users user = checkUser(update);
//        if (update.hasMessage() && update.getMessage().hasText()) {
//            String messageText = update.getMessage().getText();
//            if (messageText.equals("/start")){
//                //user = checkUser(update);
//                log.info(user.toString());
//            }
//            setlevels(user.getUserName(), 2);
//            log.info("INDO D" + user.getLevel() + " " + messageText.contains("/send"));
//            if (messageText.contains("/send") && config.getOwnerId() == chatId){
//                log.info("INFO d" + user.toString());
//                var textToSend = EmojiParser.parseToUnicode(messageText.substring(messageText.indexOf(" ")));
//                var use = userRepository.findAll();
//                for (Users users1 : use) {
//                    if (users1.getLevel() != 0) {
//                        prepareAndMessage(users1.getChatId(), textToSend);
//                    }
//                }
//            }
//        }
        add_users();
        Users rowuser = checkUser(update);
        log.info("ROWUSER = " + rowuser.toString());
        if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().equals("/start")){
            log.info("ЗАШЛИ В СТАРТ");
            //registrefUser(update.getMessage());
            rowuser = checkUser(update);
            startCommanReceived(update.getMessage().getChatId(), update);
        }
        log.info("УСЛОВИЯ ДАЛЬШЕ " + update.hasMessage() + " а " + update.getMessage().hasText() + " Э ");
        log.info("НЕРАВЕН 0" + String.valueOf(rowuser.getLevel() != 0));
        log.info("НЕРАВЕН 4" + String.valueOf(rowuser.getLevel() != 4));
        if (update.hasMessage() && update.getMessage().hasText() && rowuser.getLevel() != 0 && rowuser.getLevel() != 4) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
        log.info("ЗАШЛИ В ИФ " + messageText);
            if (messageText.contains("/send") && (rowuser.getLevel() == 2 || config.getOwnerId() == chatId)) {
                var textToSend = EmojiParser.parseToUnicode(messageText.substring(messageText.indexOf(" ")));
                var users = userRepository.findAll();
                for (Users users1 : users) {
                    prepareAndMessage(users1.getChatId(), textToSend);
                }
            }

            switch (messageText) {
//                case "/start":
//                    registrefUser(update.getMessage());
//                    startCommanReceived(chatId, update);
//                    break;
                case "/hlp":
                    prepareAndMessage(chatId, HELP_TEXT);
                    break;
                case "/list_user":
                    log.info("ЗАШЛИ В ЛИСТ " + chatId);
                    list(chatId);
                    break;
                case "/find":
                    find_user(chatId, update);
                    break;
                case "/users":
                    register(chatId, update);
                    break;
            }
        }else if (update.hasCallbackQuery() && rowuser.getLevel() != 0) {
            String callbackData = update.getCallbackQuery().getData();
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            if (callbackData.equals(YES_BUTTON)) {
                Users i = list_accept.get(messageId.toString());
                setlevels(i.getChatId());
                list_accept.remove(i.getChatId().toString());
                executeEditMessageText("Пользователь добавлен", chatId, messageId);

            } else if (callbackData.equals(NO_BUTTON)) {
                Users i = list_accept.get(messageId.toString());
                delLevels(i.getChatId());
                list_accept.remove(i.getChatId().toString());
                executeEditMessageText("Пользователь заблокирова", chatId, messageId);
            }
        }

    }
    
    private void register(long chatId, Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Список пользователей для добавления:");
        executeMessage(message);
        ArrayList<Users> lst= findLevel(update);

        if (lst.size() > 0) {
            var markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
            List<InlineKeyboardButton> row = new ArrayList<>();
            var yesButton = new InlineKeyboardButton();
            yesButton.setText("Добавить");
            yesButton.setCallbackData(YES_BUTTON);

            var noButton = new InlineKeyboardButton();
            noButton.setText("Удалить");
            noButton.setCallbackData(NO_BUTTON);

            row.add(yesButton);
            row.add(noButton);
            rowsInLine.add(row);
            markup.setKeyboard(rowsInLine);
            for (Users us : lst){
                SendMessage mess = new SendMessage();
                mess.setChatId(chatId);
                mess.setReplyMarkup(markup);
                mess.setText(us.toString());
                var d = executeMessage(mess);
                list_accept.put(d.getMessageId().toString(), us);
            }
        }else {
            SendMessage mes = new SendMessage();
            mes.setChatId(chatId);
            mes.setText("Список пуст");
            executeMessage(mes);
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    private void startCommanReceived(Long chatId, Update update) {
        String answer = "Привет " + update.getMessage().getChat().getFirstName() + " chatId = " + chatId;//+ " erh " + update.getMessage().getChat().getUserName();
        String answerw = EmojiParser.parseToUnicode(answer + " :grinning:");
        prepareAndMessage(chatId, answer);
    }


    private void registrefUser(Message message) {
        if (userRepository.findById(message.getChatId()).isEmpty()) {
            var chatId = message.getChatId();
            var chat = message.getChat();
            Users user = new Users();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegistredAd(new Timestamp(System.currentTimeMillis()));
            userRepository.save(user);
        }else {

        }
    }

    public void list(long chatId) {
        Iterable<Users> lst = userRepository.findAll();
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        for (Users users : lst) {
            message.setText(users.toString());
            executeMessage(message);
        }
    }


    private void add_users() {
        ArrayList<Users> list = new ArrayList<>();
        list.add(new Users(124124L, "first", "last", "name", 0));
        list.add(new Users(124123L, "first", "last", "name", 1));
        list.add(new Users(124122L, "first", "last", "name", 2));
        list.add(new Users(124121L, "first", "last", "name", 0));
        list.add(new Users(124120L, "first", "last", "name", 0));
        for (Users us : list) {
            userRepository.save(us);
        }
    }

    private void find_user(Long chatId, Update update) {
        boolean us = userRepository.existsById(chatId);
        Users usr = (Users) userRepository.findById(chatId).stream().findFirst().get();
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(usr.toString());
        executeMessage(message);
    }

    private void executeEditMessageText(String text, long chatId, int messageId) {
        EditMessageText messageText = new EditMessageText();
        messageText.setChatId(chatId);
        messageText.setMessageId(messageId);
        messageText.setText(text);
        try {
            execute(messageText);
        } catch (TelegramApiException s) {
            log.error(ERROR_TEXT + s.getMessage());
        }
    }

    private Message executeMessage(SendMessage message) {
        Message d = null;
        try {
            d = execute(message);
            log.info("ОТПРАВКА СООБЩЕНИЯ " + d.getMessageId() );
            Thread.sleep(500);
        } catch (TelegramApiException | InterruptedException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
        return d;
    }

    private void prepareAndMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        executeMessage(message);
    }

    @Transactional
    private Users setlevels(String user_name, int level) {
        Users users = userRepository.findByUserName(user_name);
        users.setLevel(level);
        userRepository.save(users);
        return users;
    }
    @Transactional
    private Users setlevels(long chatId) {
        Users users = userRepository.findById(chatId).get();
        users.setLevel(1);
        userRepository.save(users);
        return users;
    }

    @Transactional
    private Users delLevels(long chatId) {
        Users users = userRepository.findById(chatId).get();
        users.setLevel(4);
        users.setDeleteAd(new Timestamp(System.currentTimeMillis()));
        userRepository.save(users);
        return users;
    }

    private Users checkUser(Update update){
        Users users;
        if (userRepository.findById(update.getMessage().getChatId()).isEmpty()){
            users = new Users();
            users.setChatId(update.getMessage().getChatId());
            users.setFirstName(update.getMessage().getChat().getFirstName());
            users.setLastName(update.getMessage().getChat().getLastName());
            users.setUserName(update.getMessage().getChat().getUserName());
            users.setLevel(0);
            users.setRegistredAd(new Timestamp(System.currentTimeMillis()));
            userRepository.save(users);
        }else {
            users = (Users) userRepository.findById(update.getMessage().getChatId()).stream().findFirst().get();
            log.info("CHEK USER уже есть");
        }
        return users;
    }
    private ArrayList<Users> findLevel(Update update){
        ArrayList<Users> lst = new ArrayList<>();
        Iterable<Users> lst_us = userRepository.findByLevel(0);
        for (Users us : lst_us){
            if (us.getDeleteAd() == null) lst.add(us);
        }
        return lst;
    }
}