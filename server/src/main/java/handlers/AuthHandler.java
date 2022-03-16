package handlers;

import ru.max.server.AuthService;
import ru.max.server.FilesInformService;
import ru.max.server.MainHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.log4j.Log4j2;
import messages.AuthMessage;
import messages.FilesSizeRequest;

import java.io.IOException;


public class AuthHandler{

    private MainHandler mainHandler;
    private AuthService authService;
    private FilesInformService fileService;

    public AuthHandler(MainHandler mainHandler) {
        this.mainHandler = mainHandler;
        this.authService = mainHandler.getAuthService();
        this.fileService = mainHandler.getFilesInformService();
    }

    public void authHandle(ChannelHandlerContext ctx, Object msg) {
        String name = ((AuthMessage) msg).getLoginUser();
        String pass = ((AuthMessage) msg).getPassUser();
        String userName = authService.getNickByLoginPass(name, pass);
        if (userName != null) {
            mainHandler.setUserName(userName);
            mainHandler.getChannels().add(ctx.channel());
            try {
                ctx.writeAndFlush(new AuthMessage(userName, fileService.getListFiles(userName)));
                ctx.writeAndFlush(new FilesSizeRequest(fileService.getFilesSize(userName), fileService.getListFiles(userName)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Авторизация пройдена успешно выслан список файлов на сервере");
        } else {
            ctx.writeAndFlush(new AuthMessage("none", ""));
            System.out.println("Авторизация НЕ пройдена.");
        }
    }
}
