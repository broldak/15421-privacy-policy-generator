package com.mobile.privacy.policy.libsupport;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Interpreter;

public class Util {
    
    public static boolean isAndroidName(String name) {
        if(name.startsWith("java/") ||
           name.startsWith("javax/") ||
           name.startsWith("android/") ||
           name.startsWith("sun/") ||
           name.startsWith("com/android") ||
           name.startsWith("org/apache")) {
                return true;
            }
            
            return false;
    }
    
    public static boolean isInternetName(String name) {
        if(name.startsWith("java/net") ||
           name.startsWith("org/apache/http")) {
            return true;
           }
        
        return false;
    }
    
    public static boolean isAndroidType(Type type) {
        return isAndroidName(type.getInternalName());
    }

}
