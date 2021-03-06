/*
 * Copyright 2010 Digital Rapids Corp.
 */

/* Copyright (c) 2010 Timothy Wall, All Rights Reserved
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef.USHORT;
import com.sun.jna.ptr.ByReference;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Constant defined in WTypes.h
 *
 * @author scott.palmer
 * @author Tobias Wolf, wolf.tobias@gmx.net
 */

public interface WTypes {

    public static int CLSCTX_INPROC_SERVER = 0x1;
    public static int CLSCTX_INPROC_HANDLER = 0x2;
    public static int CLSCTX_LOCAL_SERVER = 0x4;
    public static int CLSCTX_INPROC_SERVER16 = 0x8;
    public static int CLSCTX_REMOTE_SERVER = 0x10;
    public static int CLSCTX_INPROC_HANDLER16 = 0x20;
    public static int CLSCTX_RESERVED1 = 0x40;
    public static int CLSCTX_RESERVED2 = 0x80;
    public static int CLSCTX_RESERVED3 = 0x100;
    public static int CLSCTX_RESERVED4 = 0x200;
    public static int CLSCTX_NO_CODE_DOWNLOAD = 0x400;
    public static int CLSCTX_RESERVED5 = 0x800;
    public static int CLSCTX_NO_CUSTOM_MARSHAL = 0x1000;
    public static int CLSCTX_ENABLE_CODE_DOWNLOAD = 0x2000;
    public static int CLSCTX_NO_FAILURE_LOG = 0x4000;
    public static int CLSCTX_DISABLE_AAA = 0x8000;
    public static int CLSCTX_ENABLE_AAA = 0x10000;
    public static int CLSCTX_FROM_DEFAULT_CONTEXT = 0x20000;
    public static int CLSCTX_ACTIVATE_32_BIT_SERVER = 0x40000;
    public static int CLSCTX_ACTIVATE_64_BIT_SERVER = 0x80000;
    public static int CLSCTX_ENABLE_CLOAKING = 0x100000;
    public static int CLSCTX_APPCONTAINER = 0x400000;
    public static int CLSCTX_ACTIVATE_AAA_AS_IU = 0x800000;
    public static int CLSCTX_PS_DLL = 0x80000000;
    public static int CLSCTX_SERVER = CLSCTX_INPROC_SERVER
            | CLSCTX_LOCAL_SERVER | CLSCTX_REMOTE_SERVER;
    public static int CLSCTX_ALL = CLSCTX_INPROC_SERVER | CLSCTX_INPROC_HANDLER
            | CLSCTX_LOCAL_SERVER;

    /**
     * BSTR wrapper.
     * 
     * <p>From MSDN:</p>
     * 
     * <blockquote>A BSTR (Basic string or binary string) is a string data type 
     * that is used by COM, Automation, and Interop functions. Use the BSTR data 
     * type in all interfaces that will be accessed from script.</blockquote>
     * 
     * <p>The memory structure:</p>
     * 
     * <dl>
     * <dt>Length prefix</dt>
     * <dd>Length of the data array holding the string data and does not include
     * the final two NULL characters.</dd>
     * <dt>Data string</dt>
     * <dd>UTF-16LE encoded bytes for the string.</dd>
     * <dt>Terminator</dt>
     * <dd>Two null characters</dd>
     * </dl>
     * 
     * <p>The "value" of the BSTR is the pointer to the start of the Data String,
     * the length prefix is the four bytes before that.</p>
     * 
     * <p>The MSDN states, that a BSTR derived from a Nullpointer is treated
     * as a string containing zero characters.</p>
     */
    public static class BSTR extends PointerType {
        public static class ByReference extends BSTR implements
                Structure.ByReference {
        }

        public BSTR() {
            super(Pointer.NULL);
        }

        public BSTR(Pointer pointer) {
            super(pointer);
        }

        public BSTR(String value) {
            super();
            this.setValue(value);
        }

        public void setValue(String value) {
            if(value == null) {
                value = "";
            }
            try {
                byte[] encodedValue = value.getBytes("UTF-16LE");
                // 4 bytes for the length prefix, length for the encoded data,
                // 2 bytes for the two NULL terminators
                Memory mem = new Memory(4 + encodedValue.length + 2);
                mem.clear();
                mem.setInt(0, encodedValue.length);
                mem.write(4, encodedValue, 0, encodedValue.length);
                this.setPointer(mem.share(4));
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException("UTF-16LE charset is not supported", ex);
            }
        }

        public String getValue() {
            try {
                Pointer pointer = this.getPointer();
                if(pointer == null) {
                    return "";
                }
                int stringLength = pointer.getInt(-4);
                return new String(pointer.getByteArray(0, stringLength), "UTF-16LE");
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException("UTF-16LE charset is not supported", ex);
            }
        }

        @Override
        public String toString() {
            return this.getValue();
        }
    }

    public class BSTRByReference extends ByReference {
        public BSTRByReference() {
            super(Pointer.SIZE);
        }

        public BSTRByReference(BSTR value) {
            this();
            setValue(value);
        }

        public void setValue(BSTR value) {
            this.getPointer().setPointer(0, value.getPointer());
        }

        public BSTR getValue() {
            return new BSTR(getPointer().getPointer(0));
        }

        public String getString() {
            return this.getValue().getValue();
        }
    }

    public static class LPSTR extends PointerType {
        public static class ByReference extends BSTR implements
                Structure.ByReference {
        }

        public LPSTR() {
            super(Pointer.NULL);
        }

        public LPSTR(Pointer pointer) {
            super(pointer);
        }

        public LPSTR(String value) {
            this(new Memory((value.length() + 1L) * Native.WCHAR_SIZE));
            this.setValue(value);
        }

        public void setValue(String value) {
            this.getPointer().setWideString(0, value);
        }

        public String getValue() {
            Pointer pointer = this.getPointer();
            String str = null;
            if (pointer != null)
                str = pointer.getWideString(0);

            return str;
        }

        @Override
        public String toString() {
            return this.getValue();
        }
    }

    public static class LPWSTR extends PointerType {
        public static class ByReference extends BSTR implements
                Structure.ByReference {
        }

        public LPWSTR() {
            super(Pointer.NULL);
        }

        public LPWSTR(Pointer pointer) {
            super(pointer);
        }

        public LPWSTR(String value) {
            this(new Memory((value.length() + 1L) * Native.WCHAR_SIZE));
            this.setValue(value);
        }

        public void setValue(String value) {
            this.getPointer().setWideString(0, value);
        }

        public String getValue() {
            Pointer pointer = this.getPointer();
            String str = null;
            if (pointer != null)
                str = pointer.getWideString(0);

            return str;
        }

        @Override
        public String toString() {
            return this.getValue();
        }
    }

    public static class LPOLESTR extends PointerType {
        public static class ByReference extends LPOLESTR implements
                Structure.ByReference {
        }

        public LPOLESTR() {
            super(Pointer.NULL);
        }

        public LPOLESTR(Pointer pointer) {
            super(pointer);
        }

        public LPOLESTR(String value) {
            super(new Memory((value.length() + 1L) * Native.WCHAR_SIZE));
            this.setValue(value);
        }

        public void setValue(String value) {
            this.getPointer().setWideString(0, value);
        }

        public String getValue() {
            Pointer pointer = this.getPointer();
            String str = null;
            if (pointer != null)
                str = pointer.getWideString(0);

            return str;
        }

        @Override
        public String toString() {
            return this.getValue();
        }
    }

    public static class VARTYPE extends USHORT {
        private static final long serialVersionUID = 1L;

        public VARTYPE() {
            this(0);
        }

        public VARTYPE(int value) {
            super(value);
        }
    }
    
    public static class VARTYPEByReference extends ByReference {
        public VARTYPEByReference() {
            super(VARTYPE.SIZE);
        }

        public VARTYPEByReference(VARTYPE type) {
            super(VARTYPE.SIZE);
            setValue(type);
        }
        
        public VARTYPEByReference(short type) {
            super(VARTYPE.SIZE);
            getPointer().setShort(0, type);
        }
        
        public void setValue(VARTYPE value) {
            getPointer().setShort(0, value.shortValue());
        }

        public VARTYPE getValue() {
            return new VARTYPE(getPointer().getShort(0));
        }
    }
}
