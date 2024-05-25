package arc.haldun.mylibrary.developer;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import arc.haldun.mylibrary.Tools;
import arc.haldun.mylibrary.services.filetransfer.TransferService;
import dalvik.system.DexClassLoader;

public class ExternalJARInvoker {

    private final Context context;
    private File dexFolder, destinationDEX;

    private final ArrayList<Class<?>> parameterTypes;
    private final ArrayList<Object> parameters;

    public ExternalJARInvoker(Context context) {

        parameterTypes = new ArrayList<>();
        parameters = new ArrayList<>();

        //
        // Class map
        //
        parameterTypes.add(Context.class);
        parameterTypes.add(View.class);
        parameterTypes.add(Activity.class);

        dexFolder = context.getDir("dex",0);
        destinationDEX = new File(dexFolder, "arc.dex");

        this.context = context;



    }

    protected void addParameter(Object parameter) {
        parameters.add(parameter);
    }

    protected void start() {

        Tools.makeText(context, "DEX indiriliyor...");

        new Thread(() -> {

            TransferService transferService = new TransferService();

            transferService.setOnCompleteListener(() -> {

                Tools.makeText(context, "Kod yürütülüyor");

                try {

                    DexClassLoader dexClassLoader = new DexClassLoader(
                            destinationDEX.getAbsolutePath(),
                            context.getDir("dex", 0).getAbsolutePath(),
                            null,
                            context.getClassLoader());

                    Class<?> loadedClass = dexClassLoader.loadClass("arc.reflection.Entry");
                    Object instance = loadedClass.getDeclaredConstructor().newInstance();
                    Method method = loadedClass.getMethod("start", parameterTypes.toArray(new Class[0]));
                    method.invoke(instance, parameters.toArray());

                } catch (ClassNotFoundException |
                         NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                         InstantiationException e) {
                    Tools.startErrorActivity(context, e);
                }
            });

            transferService.downloadFile("http://haldun.online/reflection/arc.dex", destinationDEX.getAbsolutePath());
        }).start();

    }
}
