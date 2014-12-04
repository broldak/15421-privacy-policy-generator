package com.mobile.privacy.policy.libsupport;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;

import com.mobile.privacy.policy.parser.ClassResolver;
public class PrivateMethodAnalyzer {

    public static void parseClass(byte[] classData, final FieldPool fieldPool, final ClassResolver cr) {
        final Map<String,Set<String>> results = new HashMap<String,Set<String>>();
        //final LibraryAnalyser la = new LibraryAnalyser();
        //la.loadMapping("3rdPartyLibraries.txt");
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM5) {
            private String className;
            /**
             * Called when a class is visited. This is the method called first
             */
            @Override
            public void visit(int version, int access, String name,
                    String signature, String superName, String[] interfaces) {
                className = name;
                cr.addClass(name,superName);
                super.visit(version, access, name, signature, superName, interfaces);
            }
            
            @Override
            public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
                int tag = (access & Opcodes.ACC_STATIC) != 0 ? Opcodes.H_GETSTATIC : Opcodes.H_GETFIELD;
                Handle handle = new Handle(tag,className,name,desc);
                DataValue dataValue = null;
                if(value == null) {
                    dataValue = DataInterpreter.typeToDataValue(Type.getType(desc));
                }
                else if(value instanceof Integer) {
                    dataValue = new NativeValue(1,value);
                } else if (value instanceof Float) {
                    dataValue = NativeValue.WORD_VALUE;
                } else if (value instanceof Long) {
                    dataValue = new NativeValue(2,value);
                } else if (value instanceof Double) {
                    dataValue = NativeValue.DOUBLE_VALUE;
                } else if (value instanceof String) {
                    dataValue = new SimpleObjectValue();
                    ((SimpleObjectValue) dataValue).stringValues.add((String)value);
                }
                
                fieldPool.initField(handle, dataValue);
                
                return null;
            }
            
            

            /**
             * When a method is encountered
             */
            @Override
            public MethodVisitor visitMethod(int access, String name,
                    String desc, String signature, String[] exceptions) {
                
                MethodVisitor mv = new MethodVisitor(Opcodes.ASM5) {
                    
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                        if(name.contains("getExternalStoragePublicDirectory")) {
                            System.out.println("WRITE");
                        }
                        if(owner.equals("android/hardware/Camera") || owner.contains("android/hardware/camera2")) {
                            System.out.println("CAMERA");
                            /*
                            if(!la.lookupPackgeName(className).getValue().equals("ANDROID")) {
                                if(results.get("CAMERA") == null) {
                                    results.put("CAMERA", new HashSet<String>());
                                }
                                results.get("CAMERA").add(la.lookupPackgeName(className).getValue());
                            }
                            */
                        } else if(owner.equals("android/location/LocationManager")) {
                            System.out.println("LOCATION");
                            /*
                            if(!la.lookupPackgeName(className).getValue().equals("ANDROID")) {
                                if(results.get("LOCATION") != null) {
                                    results.get("LOCATION").add(la.lookupPackgeName(className).getValue());
                                }
                                results.get("LOCATION").add(la.lookupPackgeName(className).getValue());
                            }
                            */
                        } else if(owner.equals("android/telephony/SmsManager")) {
                            System.out.println("SMS_SEND");
                            /*
                            if(!la.lookupPackgeName(className).getValue().equals("ANDROID")) {
                                if(results.get("SMS_SEND") == null) {
                                    results.put("SMS_SEND", new HashSet<String>());
                                }
                                results.get("SMS_SEND").add(la.lookupPackgeName(className).getValue());
                            }
                            */
                        }
                    }
                    
                    @Override
                    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                        if(owner.contains("android/provider/ContactsContract")) {
                            System.out.println("CONTACTS");
                            /*
                            if(!la.lookupPackgeName(className).getValue().equals("ANDROID")) {
                                if(results.get("CONTACTS") == null) {
                                    results.put("CONTACTS", new HashSet<String>());
                                }
                                results.get("CONTACTS").add(la.lookupPackgeName(className).getValue());
                            }
                            */
                        }
                        else if(owner.contains("android/provider/MediaStore/Images")) {
                            System.out.println("IMAGES");
                            /*
                            if(!la.lookupPackgeName(className).getValue().equals("ANDROID")) {
                                if(results.get("IMAGE_SEND") == null) {
                                    results.put("IMAGE_SEND", new HashSet<String>());
                                }
                                results.get("IMAGE_SEND").add(la.lookupPackgeName(className).getValue());
                            }
                            */
                        }
                    }
                    
                    @Override
                    public void visitLdcInsn(Object cst) {
                        if(cst instanceof String) {
                            String con = (String) cst;
                            if(con.contains("content://sms/inbox")) {
                                System.out.println("sms read");
                                /*
                                if(!la.lookupPackgeName(className).getValue().equals("ANDROID")) {
                                    if(results.get("SMS_READ") == null) {
                                        results.put("SMS_READ", new HashSet<String>());
                                    }
                                    results.get("SMS_READ").add(la.lookupPackgeName(className).getValue());
                                }
                                */
                            }
                            
                        }
                    }
                };
                return null; //mv;
            }

            /**
             * When the optional source is encountered
             */
            @Override
            public void visitSource(String source, String debug) {
                System.out.println("Source: "+source);
                super.visitSource(source, debug);
            }
            
            
        };
        ClassReader classReader;
        classReader = new ClassReader(classData);
        classReader.accept(cv, 0);
    }
    
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
