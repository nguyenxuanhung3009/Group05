package finalProject;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.tritonus.share.sampled.file.TAudioFileFormat;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import javafx.embed.swing.JFXPanel;

@SuppressWarnings("serial")
public class playMp3 extends JFrame {

	public static JFrame frame;
	public static JTable table;
	public static MediaPlayer play;
	public static int dem;
	public static JTextField sub;
	public String[] columNames = { "Time", "Sub" };
	public static String time1;
	public static String time2;
	public static int rowSub;
	public static int lastRow;
	public static String beginTime1;
	public static String endTime1;
	public static String endTime;
	public static int demThread;
	public static Duration a;
	public static Duration b;
	public static DefaultTableModel model;
	public static Thread thread, thread1;
	public static Object[][] data = {};

	public playMp3() {
		createAndShowGUI();
	}

	public void createAndShowGUI() {
		frame = new JFrame("Play MP3");
		frame.setSize(600, 440);
		frame.setLayout(new FlowLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JButton btnMp3 = new JButton("Browse MP3 File");
		btnMp3.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.showOpenDialog(null);
				File chosenSoundFile = fileChooser.getSelectedFile();

				AudioFileFormat fileFormat;
				try {
					fileFormat = AudioSystem.getAudioFileFormat(chosenSoundFile);

					Map<?, ?> properties = ((TAudioFileFormat) fileFormat).properties();
					String key = "duration";
					Long microseconds = (Long) properties.get(key);
					int mili = (int) (microseconds / 1000);
					int sec = (mili / 1000) % 60;
					int min = (mili / 1000) / 60;
					String endTimeString = min + ":" + sec;
					endTime = endTimeString;
				} catch (UnsupportedAudioFileException | IOException e1) {
					e1.printStackTrace();
				}

				String me = "file:///" + ("" + chosenSoundFile).replace("\\", "/").replaceAll(" ", "%20");

				Media m = new Media(me);
				play = new MediaPlayer(m);

				if (dem == 2) {
					play.play();
					subTitle();
				}

				dem = 1;
			}

		});
		frame.add(btnMp3);

		JButton btnExcel = new JButton("Browse Excel File");
		btnExcel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e1) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.showOpenDialog(null);
				File chosenExcelFile = fileChooser.getSelectedFile();
				String link = chosenExcelFile.getAbsolutePath();
				playMp3.getDataFromExcel(link);

				if (dem == 1) {
					play.play();
					subTitle();
				}
				dem = 2;
			}
		});
		frame.add(btnExcel);

		table = new JTable(data, columNames);
		table.setPreferredScrollableViewportSize(new Dimension(500, 300));
		table.setFillsViewportHeight(true);
		table.addMouseListener(new java.awt.event.MouseAdapter() {

			@SuppressWarnings("deprecation")
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				int row = table.rowAtPoint(evt.getPoint());

				rowSub = row;
				if (row == lastRow - 1) { // chu y
					beginTime1 = (String) data[row][0];
					endTime1 = endTime;
				} else {
					beginTime1 = (String) data[row][0];
					endTime1 = endTime;
				}
				a = new Duration(timeString2milisecont(beginTime1));
				b = new Duration(timeString2milisecont(endTime1));

				play.setStartTime(a);
				play.setStopTime(b);

				if (dem == 1 || dem == 2)
					play.stop();
				play.play();

				if (demThread == 1) {
					if (thread.isAlive()) {
						thread.stop();
						thread1.start();
						demThread = 2;
					} else
						subTitle();
				} else if (demThread == 2) {
					thread1.stop();
					subTitle();
				} else
					subTitle();
			}

		});
		JScrollPane scrollPane = new JScrollPane(table);
		frame.add(scrollPane);

		sub = new JTextField();
		frame.add(sub);
		sub.setColumns(45);
		frame.setVisible(true);
	}

	public static double timeString2milisecont(String timeinString) {
		// String string = "004-034556";
		String[] parts = timeinString.split(":");
		String phut = parts[0]; // 004
		String giay = parts[1]; // 034556
		int intPhut = Integer.parseInt(phut);
		int intGiay = Integer.parseInt(giay);
		return intPhut * 60000 + intGiay * 1000;
	}

	public static void getDataFromExcel(String pathExcel) {

		File file = new File(pathExcel);
		try {
			Workbook wk = Workbook.getWorkbook(file);
			Sheet sheet = wk.getSheet(0);
			int rows = sheet.getRows();
			int cols = sheet.getColumns();
			data = new Object[rows][cols];
			lastRow = rows;
			for (int row = 0; row < rows; row++) {
				for (int col = 0; col < cols; col++) {
					Cell cell = sheet.getCell(col, row);
					data[row][col] = cell.getContents();

				}
				String[] columnNames = { "Time", "Sub" };
				model = new DefaultTableModel(data, columnNames);
				table.setModel(model);
			}
			wk.close();
		} catch (BiffException e1) {
		} catch (IOException e) {
		}
	}

	public static void subTitle() {
		thread = new Thread(new Runnable() {

			@Override
			public void run() {
				demThread = 1;
				for (int i = rowSub; i < lastRow; i++) {

					if (i == lastRow - 1) { //
						time1 = (String) data[i][0];
						time2 = endTime;

					} else {
						time1 = (String) data[i][0];
						time2 = (String) data[i + 1][0];
					}
					sub.setText((String) data[i][1]);

					Rectangle cellRect = table.getCellRect(i, 0, true);
					table.scrollRectToVisible(cellRect);
					try {

						Thread.sleep((long) (timeString2milisecont(time2) - timeString2milisecont(time1)));
					} catch (InterruptedException e) {

					}

				}

			}

		});
		thread.start();
		thread1 = new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = rowSub; i < lastRow; i++) {

					if (i == lastRow - 1) { //
						time1 = (String) data[i][0];
						time2 = endTime;

					} else {
						time1 = (String) data[i][0];
						time2 = (String) data[i + 1][0];
					}
					sub.setText((String) data[i][1]);

					Rectangle cellRect = table.getCellRect(i, 0, true);
					table.scrollRectToVisible(cellRect);
					try {

						Thread.sleep((long) (timeString2milisecont(time2) - timeString2milisecont(time1)));
					} catch (InterruptedException e) {

					}
				}

			}

		});

	}

	public static void main(String[] args) {
		new JFXPanel();
		new playMp3();
	}

}
