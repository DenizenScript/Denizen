package com.denizenscript.denizen.utilities.packets;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.codegen.CodeGenUtil;
import org.objectweb.asm.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

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
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", Type.getConstructorDescriptor(origConstructor), null, null);
            mv.visitCode();
            Label startLabel = new Label();
            mv.visitLabel(startLabel);
            mv.visitLineNumber(0, startLabel);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(denClass), "<init>", Type.getConstructorDescriptor(origConstructor), false);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitLocalVariable("this", "L" + className + ";", null, startLabel, startLabel, 0);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
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
                        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, method.getName(), Type.getMethodDescriptor(method), null, null);
                        mv.visitCode();
                        startLabel = new Label();
                        mv.visitLabel(startLabel);
                        mv.visitLineNumber(0, startLabel);
                        mv.visitVarInsn(Opcodes.ALOAD, 0);
                        mv.visitFieldInsn(Opcodes.GETFIELD, Type.getInternalName(abstractClass), "oldListener", Type.getDescriptor(nmsClass));
                        int id = 1;
                        for (Class<?> type : method.getParameterTypes()) {
                            if (NMSHandler.debugPackets) {
                                Debug.log("Var " + id + " is type " + type.getName());
                            }
                            int index = id++;
                            if (type == int.class || type == boolean.class || type == short.class || type == char.class) { mv.visitVarInsn(Opcodes.ILOAD, index); } // Everything sub-integer-width is secretly integers
                            else if (type == long.class) { mv.visitVarInsn(Opcodes.LLOAD, index); id++; }
                            else if (type == float.class) { mv.visitVarInsn(Opcodes.FLOAD, index); }
                            else if (type == double.class) { mv.visitVarInsn(Opcodes.DLOAD, index); id++; } // Doubles and longs have two vars secretly for some reason
                            else { mv.visitVarInsn(Opcodes.ALOAD, index); }
                        }
                        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(nmsClass), method.getName(), Type.getMethodDescriptor(method), false);
                        int returnCode = Opcodes.ARETURN;
                        Class<?> returnType = method.getReturnType();
                        if (returnType == int.class || returnType == boolean.class || returnType == short.class || returnType == char.class) { returnCode = Opcodes.IRETURN; }
                        else if (returnType == long.class) { returnCode = Opcodes.LRETURN; }
                        else if (returnType == float.class) { returnCode = Opcodes.FRETURN; }
                        else if (returnType == double.class) { returnCode = Opcodes.DRETURN; }
                        else if (returnType == void.class) { returnCode = Opcodes.RETURN; }
                        mv.visitInsn(returnCode);
                        mv.visitLocalVariable("this", "L" + className + ";", null, startLabel, startLabel, 0);
                        id = 1;
                        for (Class<?> type : method.getParameterTypes()) {
                            mv.visitLocalVariable("var" + id, Type.getDescriptor(type), null, startLabel, startLabel, id);
                            if (type == double.class || type == long.class) {
                                id++;
                            }
                            id++;
                        }
                        mv.visitMaxs(0, 0);
                        mv.visitEnd();
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
