package com.example.ricardo.taller2;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by Ricardo on 04/09/2017.
 */

public class ContactsCursor extends CursorAdapter{

    private static final int CONTACT_ID_INDEX = 0;
    private static final int DISPLAY_NAME_INDEX = 1;

    public ContactsCursor(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.contact_row,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvIdContacto = (TextView) view.findViewById(R.id.idContacto);
        TextView tvNombre = (TextView) view.findViewById(R.id.nameContacto);

        int idnum = cursor.getInt(CONTACT_ID_INDEX);
        String nombre = cursor.getString(DISPLAY_NAME_INDEX);

        tvIdContacto.setText(String.valueOf(idnum));
        tvNombre.setText(nombre);

    }
}
