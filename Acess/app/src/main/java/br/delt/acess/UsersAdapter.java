package br.delt.acess;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class UsersAdapter extends ArrayAdapter<User>
{
    public UsersAdapter(Context context, List<User> users)
    {
        super(context,0,users);

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {

        User user =getItem(position);
        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_cell, parent, false);

        //Inserir a procura das views dos fields
        ImageView image = convertView.findViewById( R.id.cellImage);
        TextView nome = convertView.findViewById( R.id.cellName);
        TextView cargo = convertView.findViewById( R.id.cellCargo);
        TextView  lastAcess = convertView.findViewById( R.id.cellLastAcess);


        //Inserir o preenchimento das views dos fields
        try {
            int flag = Intent.FLAG_GRANT_READ_URI_PERMISSION;
            convertView.getContext().getContentResolver().takePersistableUriPermission(Uri.parse(user.getPictureURI()), flag);
            image.setImageBitmap(MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), Uri.parse(user.getPictureURI())));
        } catch (IOException e) {

        }
        catch (Exception e) {}
        nome.setText(user.getNome());
        cargo.setText(user.getCargo());
        lastAcess.setText(SQLiteManager.getStringFromDate(user.getLastAcess()));

        return  convertView;

    }
}
