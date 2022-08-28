package com.denizenscript.denizen.utilities.packets;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.codegen.CodeGenUtil;
import com.denizenscript.denizencore.utilities.codegen.MethodGenerator;
import org.objectweb.asm.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

public class NetworkInterceptCodeGen {

    public static MethodHandle generateInstance = null;

    public static void generateClass(Class<?> denClass, Class<?> abstractClass, Class<?> nmsClass) {
        try {
            Constructor<?> origConstructor = denClass.getConstructors()[0];
            // ====== Build class ======
            String className = "com/denizenscript/denizen/network_intercept_codegen/GeneratedInterceptor";
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className, null, Type.getInternalName(denClass), new String[0]);
            cw.visitSource("GENERATED_INTERCEPTOR", null);
            // ====== Build constructor ======
            {
                MethodGenerator gen = MethodGenerator.generateMethod(className, cw, Opcodes.ACC_PUBLIC, "<init>", Type.getConstructorDescriptor(origConstructor));
                gen.loadThis();
                int p = 0;
                for (Parameter param : origConstructor.getParameters()) {
                    MethodGenerator.Local local = gen.addLocal("arg" + (p++), param.getType());
                    gen.loadLocal(local);
                }
                gen.invokeSpecial(origConstructor);
                gen.returnNone();
                gen.end();
            }
            // ====== Find public methods ======
            for (Method method : nmsClass.getDeclaredMethods()) {
                int modifier = method.getModifiers();
                if (Modifier.isPublic(modifier) && !Modifier.isFinal(modifier) && !Modifier.isStatic(modifier)) {
                    boolean hasMethod = false;
                    try {
                        abstractClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
                        hasMethod = true;
                    }
                    catch (NoSuchMethodException ignore) {}
                    if (!hasMethod) {
                        if (NMSHandler.debugPackets) {
                            Debug.log("Must override " + method + " --- " + method.getName() + ", returns " + method.getReturnType() + " is " + modifier
                                    + ", Public=" + Modifier.isPublic(modifier) + ", final=" + Modifier.isFinal(modifier));
                        }
                        MethodGenerator gen = MethodGenerator.generateMethod(className, cw, Opcodes.ACC_PUBLIC, method.getName(), Type.getMethodDescriptor(method));
                        gen.loadThis();
                        gen.loadInstanceField(Type.getInternalName(abstractClass), "oldListener", Type.getDescriptor(nmsClass));
                        int id = 1;
                        for (Class<?> type : method.getParameterTypes()) {
                            if (NMSHandler.debugPackets) {
                                Debug.log("Var " + id + " is type " + type.getName());
                            }
                            MethodGenerator.Local local = gen.addLocal("arg_" + (id++), type);
                            gen.loadLocal(local);
                        }
                        gen.invokeVirtual(method);
                        gen.returnValue(method.getReturnType());
                        gen.end();
                    }
                }
            }
            // ====== Compile and return ======
            cw.visitEnd();
            byte[] compiled = cw.toByteArray();
            Class<?> generatedClass = CodeGenUtil.loader.define(className.replace('/', '.'), compiled);
            Constructor<?> ctor = generatedClass.getDeclaredConstructor(origConstructor.getParameterTypes());
            ctor.setAccessible(true);
            generateInstance = MethodHandles.lookup().unreflectConstructor(ctor);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    public static Object generateAppropriateInterceptor(Object netMan, Object player, Class<?> denClass, Class<?> abstractClass, Class<?> nmsClass) {
        if (generateInstance == null) {
            generateClass(denClass, abstractClass, nmsClass);
        }
        try {
            return generateInstance.invoke(netMan, player);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
            return null;
        }
    }
}
