package me.iscle.becatimer;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private TextView totalTimeTV;
    private TextView currentTimeTV;
    private Button startTimerBtn;
    private Button exportBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        totalTimeTV = findViewById(R.id.total_time_text_view);
        currentTimeTV = findViewById(R.id.current_time_text_view);
        startTimerBtn = findViewById(R.id.start_timer_button);
        exportBtn = findViewById(R.id.export_button);

        initButtons();
        initTextViews();
    }

    private void initButtons() {
        startTimerBtn.setOnClickListener(v -> {
            createNotification();
        });

        exportBtn.setOnClickListener(v -> {
            exportCurrentTime();
        });
    }

    private void initTextViews() {
        totalTimeTV.setText("69.3");
        currentTimeTV.setText("34.6");
    }

    private void exportCurrentTime() {
        new ExportXLSX().execute();
    }

    private void resetCurrentHours() {
        Toast.makeText(this, "Your current time has been reset successfully!", Toast.LENGTH_LONG).show();
    }

    private void createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "TIMER_CHANNEL")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Hours are being updated...")
                .setContentText("Tap to open the app")
                .setUsesChronometer(true)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());

    }

    private class ExportXLSX extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                Workbook workbook = WorkbookFactory.create(getResources().openRawResource(R.raw.template_time));
                Sheet sheet = workbook.getSheetAt(0);
                sheet.getRow(0).getCell(0).setCellValue("Name goes here");
                Row baseRow = sheet.getRow(2);
                sheet.getRow(1).setRowStyle(baseRow.getRowStyle());
                sheet.shiftRows(4, 4, 1);

                FileOutputStream xlsx = new FileOutputStream(getXLSXFile());
                workbook.write(xlsx);
                xlsx.close();
                return true;
            } catch (IOException | InvalidFormatException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Reset current time to 0?")
                        .setMessage("Your current time has been exported successfully. Do you want to reset it to 0?\nYour total time won't be affected by this.")
                        .setPositiveButton("Yes", (dialog, which) -> resetCurrentHours())
                        .setNegativeButton("No", null)
                        .create()
                        .show();
            } else {
                Toast.makeText(MainActivity.this, "There was an error while exporting your current time!", Toast.LENGTH_LONG).show();
            }
        }

        private File getXLSXFile() {
            File folder = new File(Environment.getExternalStorageDirectory(), "BecaTimer");
            folder.mkdir();
            File xlsx = new File(folder.getAbsolutePath(), "test.xlsx");
            try {
                xlsx.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return xlsx;
        }

        private void addNewRow(Sheet sheet, int base, int dest) {

        }
    }
}
