package com.example.chocoasus.dial;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.provider.ContactsContract.CommonDataKinds;
import android.database.Cursor;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_SELECT_PHONE_NUMBER = 1;

    private SharedPreferences sharedPref ;//= getSharedPreferences("Sett", MODE_PRIVATE);
    private SharedPreferences.Editor editor ;//= sharedPref.edit();

    private final ArrayList <String> cardArray = new ArrayList<>();
    private final ArrayList<Contact> recentArrayList = new ArrayList<>();

    private CustomAutoCompleteTextView cardAutoText ;//= (CustomAutoCompleteTextView) findViewById(R.id.crd);
    private ListView recentListView ;//= (ListView) findViewById(R.id.lst);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = getSharedPreferences("Sett", MODE_PRIVATE);
        editor = sharedPref.edit();

        cardAutoText = (CustomAutoCompleteTextView) findViewById(R.id.crd);
        recentListView = (ListView) findViewById(R.id.lst);

        //-----------------------------IMAGE--------------------------------
        ImageView image = (ImageView) findViewById(R.id.imageView);
        image.setImageResource(R.drawable.batelco);
        //-----------------------------CARD NUMBER TEXT--------------------------------
        SharedPreferences oldsharedPref = getSharedPreferences("mySettings", MODE_PRIVATE);
        SharedPreferences.Editor oldeditor = oldsharedPref.edit();
        String oldcard = oldsharedPref.getString("mySetting", "");
        if(sharedPref.getString("crd", "").equals("")){ editor.putString("crd", oldcard); editor.apply();}

        // Construct the card numbers data source
        ArrayAdapter<String> cardAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, cardArray);
        try{
            for (int i = 0;i<3; ++i){
                final String str = sharedPref.getString(String.valueOf(Integer.valueOf(i)), "");
                if (!str.equals("")){
                    cardArray.add(str);
                } else {
                    if(!oldcard.equals("")){cardArray.add(oldcard);oldeditor.remove("mySetting");oldeditor.clear();oldeditor.apply();}
                    break; // Empty String means the default value was returned.
                }
            }



            cardAdapter.notifyDataSetChanged();} catch (ActivityNotFoundException ignored){}

        // Attach the recentAdapter to a ListView
        cardAutoText.setAdapter(cardAdapter);

        cardAutoText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                cardAutoText.showDropDown();
                if(cardAutoText.getError()!=null)cardAutoText.setError(null);
                return false;
            }
        });

        cardAutoText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                ArrayAdapter<String> cardAdapter = (ArrayAdapter<String>) cardAutoText.getAdapter();

                String crdtxt = (String) arg0.getItemAtPosition(arg2);
                cardAdapter.notifyDataSetChanged();

                editor.putString("crd", crdtxt);
                editor.apply();

                InputMethodManager imm = (InputMethodManager) cardAutoText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(cardAutoText.getWindowToken(), 0);
                cardAutoText.clearFocus();

                Toast.makeText(cardAutoText.getContext(),"Number Is Updated" , Toast.LENGTH_SHORT).show();
            }
        });

       // Initialize Card Number Text Field
       cardAutoText.setText(sharedPref.getString("crd", null));

       cardAutoText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                String saved = sharedPref.getString("crd", "");
                String current = cardAutoText.getText().toString();

                if (keyCode == KeyEvent.KEYCODE_BACK && !current.equals(saved)) {
                    cardAutoText.setText(saved);
                    Toast.makeText(cardAutoText.getContext(),"Number Not Saved" , Toast.LENGTH_SHORT).show();
                    return true;
                }   return false;
            }
        });

        cardAutoText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    final ArrayAdapter<String> cardAdapter = (ArrayAdapter<String>) cardAutoText.getAdapter();

                    final String crdtxt = cardAutoText.getText().toString();

                    if (crdtxt.length() == 0) {
                        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        v.clearFocus();
                        return true;
                    }

                    AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
                    adb.setTitle("Save?");
                    adb.setMessage("Would you like to save this number?");
                    adb.setNegativeButton("Cancel", null);
                    adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            cardArray.add(0, crdtxt);
                            cardAdapter.insert(crdtxt, 0);
                            cardAdapter.notifyDataSetChanged();

                            editor.putString("crd", crdtxt);
                            editor.apply();

                            Toast.makeText(getApplicationContext(), "Card Number Added", Toast.LENGTH_SHORT).show();

                            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                            v.clearFocus();
                        }
                    });
                    adb.show();
                    return true;
                }
                return false;
            }
        });

        //-----------------------------DIAL CONTACT BUTTON--------------------------------
        // Handle Dial Contact Button Clicked
        Button myButton = (Button) findViewById(R.id.button);
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start an activity for the user to pick a phone number from contacts
                if (cardAutoText.getText().toString().length() == 0) {
                    cardAutoText.setError("Please Enter a Valid Card Number First!");
                    cardAutoText.requestFocus();
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType(CommonDataKinds.Phone.CONTENT_TYPE);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, REQUEST_SELECT_PHONE_NUMBER);
                    }
                }
            }
        });
        //-----------------------------RECENT CONTACTS--------------------------------

        try{
            for (int i = 0; i < 10; ++i){
                String name= sharedPref.getString("Name_" + String.valueOf(Integer.valueOf(i)), "");
                String tel=  sharedPref.getString("Tel_"+String.valueOf(Integer.valueOf(i)), "");
                if (!name.equals("")){
                    recentArrayList.add(new Contact(name,tel));
                } else {
                    break; // Empty String means the default value was returned.
                }
            }
        } catch (ActivityNotFoundException ignored){}

        final ContactsAdapter recentAdapter = new ContactsAdapter(this, recentArrayList);

        // Attach the recentAdapter to a ListView
        recentListView.setAdapter(recentAdapter);

        // Attach Event Handlers
        recentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (cardAutoText.getText().toString().length() == 0) {
                    cardAutoText.setError("Please Enter a Valid Card Number First!");
                    cardAutoText.requestFocus();
                } else {
                String tel_number =   recentArrayList.get(position).tel;
                String tel_name =   recentArrayList.get(position).name;

                Toast.makeText(getApplicationContext(),
                        "Calling Number: " + tel_name , Toast.LENGTH_LONG)
                        .show();

                tel_number=tel_number.replaceAll("[^0-9]", "");
                if(tel_number.startsWith("00")){tel_number=tel_number.substring(2);}

                String card_num = cardAutoText.getText().toString();
                String batelco_mask =  "80008888,,"+card_num+"#,,,123";

                try {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:"+Uri.encode(batelco_mask+tel_number+"#") ));
                    startActivity(callIntent);
                } catch (ActivityNotFoundException ignored) {}
            }}
        });

        recentListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {

                AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
                adb.setTitle("Delete?");
                adb.setMessage("Are you sure you want to delete " + recentAdapter.getItem(position).name);
                final int positionToRemove = position;
                adb.setNegativeButton("Cancel", null);
                adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        recentAdapter.remove(recentAdapter.getItem(positionToRemove));
                        recentAdapter.notifyDataSetChanged();
                    }
                });
                adb.show();
                return false;
            }
        });
        //----------------------------------------------------------------------------
    }// End onCreate
//-----------------------------------------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_SELECT_PHONE_NUMBER && resultCode == RESULT_OK) {
            // Get the URI and query the content provider for the phone number
            Uri contactUri = data.getData();
            String[] projection = new String[]{CommonDataKinds.Phone.DISPLAY_NAME,CommonDataKinds.Phone.NUMBER};
            Cursor cursor = getContentResolver().query(contactUri, projection,
                    null, null, null);
            // If the cursor returned is valid, get the phone number
            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER);
                int nameIndex = cursor.getColumnIndex(CommonDataKinds.Phone.DISPLAY_NAME);

                String tel_number = cursor.getString(numberIndex);
                String tel_name = cursor.getString(nameIndex);
                // Do something with the phone number
                tel_number=tel_number.replaceAll("[^0-9]", "");
                if(tel_number.startsWith("00")){tel_number=tel_number.substring(2);}

                String card_num = cardAutoText.getText().toString();
                String batelco_mask =  "80008888,,"+card_num+"#,,,123";

                try {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:"+Uri.encode(batelco_mask+tel_number+"#") ));
                    startActivity(callIntent);

                    final ContactsAdapter recentAdapter = (ContactsAdapter) recentListView.getAdapter();
                    recentArrayList.add(0, new Contact(tel_name,tel_number));
                    recentAdapter.notifyDataSetChanged();

                    Toast.makeText(getApplicationContext(),
                            "Calling Number: " + tel_name , Toast.LENGTH_LONG)
                            .show();

                } catch (ActivityNotFoundException ignored) {}
            } cursor.close();
        }
    }//End onActivityResult

    // ----------------------------------------------------------------------------------

    @Override
    public void onPause() {
        super.onPause();

        int cardLoop = 3;
        if (cardArray.size()<3){cardLoop = cardArray.size();}
        for (int i = 0; i < cardLoop; ++i){
            try{editor.putString(String.valueOf(i), cardArray.get(i));}
            catch (ActivityNotFoundException activityException){activityException.printStackTrace();}}

        int recentLoop = recentArrayList.size();
        if (recentLoop > 10){recentLoop = 10;}
        for (int i = 0; i < recentLoop; ++i){
            try{
                editor.putString("Name_" + String.valueOf(i), recentArrayList.get(i).name);
                editor.putString("Tel_"+String.valueOf(i),  recentArrayList.get(i).tel);
            } catch (ActivityNotFoundException activityException){activityException.printStackTrace();}}

        for (int i = recentArrayList.size(); i < 10; ++i){
            try{
                editor.remove("Name_" + String.valueOf(i));
                editor.remove("Tel_" + String.valueOf(i));
            } catch (ActivityNotFoundException activityException){activityException.printStackTrace();}}

        editor.apply();
    }//End onPause

}//End Class

//----------------------------------------------------------------------------------------

class Contact {
    public final String name;
    public final String tel;

    public Contact(String name, String tel) {
        this.name = name;
        this.tel = tel;
    }
}

class ContactsAdapter extends ArrayAdapter<Contact> {
    public ContactsAdapter(Context context, ArrayList<Contact> contacts) {
        super(context, 0, contacts);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Contact contact = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.contact_item, parent, false);
        }
        // Lookup view for data population
        TextView cName = (TextView) convertView.findViewById(R.id.cName);
        TextView cTel = (TextView) convertView.findViewById(R.id.cTel);
        // Populate the data into the template view using the data object
        cName.setText(contact.name);
        cTel.setText(contact.tel);
        // Return the completed view to render on screen
        return convertView;
    }
}