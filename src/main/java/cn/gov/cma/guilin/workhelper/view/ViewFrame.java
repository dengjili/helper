package cn.gov.cma.guilin.workhelper.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.io.File;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cn.gov.cma.guilin.workhelper.controller.Controller;

public class ViewFrame {

	final int WIDTH = 800;
	final int HEIGHT = 400;

	private JFrame frame = new JFrame();
	private ViewPanel panel = new ViewPanel();

	private class ViewPanel extends JPanel {
		private static final long serialVersionUID = -8829939534455410766L;

		public void add(Component c, GridBagConstraints constraints, int x, int y, int w, int h) {
			constraints.gridx = x;
			constraints.gridy = y;
			constraints.gridwidth = w;
			constraints.gridheight = h;
			add(c, constraints);
		}
	}

	public void show() {
		initViewFrame();
		setViewFrameLayout();
		frame.setVisible(true);
	}

	private void setViewFrameLayout() {
		JButton converButton = new JButton("开始生成");
		JButton originButton = new JButton("导入值班日志");
		JButton destButton = new JButton("文件另存为");
		JTextField originField = new JTextField(50);
		JTextField destField = new JTextField(50);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 12; // 指定如何分布额外的水平空间
		constraints.weighty = 3; // 指定如何分布额外的垂直空间
		panel.add(originButton, constraints, 0, 0, 3, 1);
		panel.add(originField, constraints, 4, 0, 8, 1);
		panel.add(destButton, constraints, 0, 1, 3, 1);
		panel.add(destField, constraints, 4, 1, 8, 1);
		panel.add(converButton, constraints, 0, 4, 4, 1);

		// 注册监听事件
		originButton.addActionListener((event) -> {
			JFileChooser chooser = new JFileChooser();
			chooser.showOpenDialog(panel);// 显示打开的文件对话框
			File f = chooser.getSelectedFile();// 使用文件类获取选择器选择的文件
			String path = f.getAbsolutePath();// 返回路径名
			originField.setText(path);
		});
		destButton.addActionListener((event) -> {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);// 设定只能选择到文件夹
			chooser.showOpenDialog(panel);// 显示打开的文件对话框
			File f = chooser.getSelectedFile();// 使用文件类获取选择器选择的文件
			String path = f.getAbsolutePath();// 返回路径名
			destField.setText(path);
		});
		converButton.addActionListener((event) -> {
			String originPath = originField.getText();
			String destPath = destField.getText();
			
			Controller controller = new Controller(originPath.trim(), destPath.trim());
			Map<String, String> result = controller.doConver();
			
			String flag = result.get("flag");
			String message = result.get("message");
			
			if ("0".equals(flag)) {
				JOptionPane.showMessageDialog(panel, message, "提示信息",JOptionPane.INFORMATION_MESSAGE); 
			} else {
				JOptionPane.showMessageDialog(panel, message, "提示信息",JOptionPane.WARNING_MESSAGE); 
			}
			
		});

	}

	private void initViewFrame() {
		frame.setTitle("桂林高空探测质量统计表生成工具");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// 设置其顶层容器的关闭性
		frame.add(panel, BorderLayout.WEST); // 添加一个面板
		panel.setLayout(new GridBagLayout()); // 对区域进行网络划分，不同之处在于，组件可以占据一个网格，也可以占据几个网格。

		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		int width = screenSize.width;
		int height = screenSize.height;
		int x = (width - WIDTH) / 2;
		int y = (height - HEIGHT) / 2;
		frame.setSize(WIDTH, HEIGHT); // 设置默认大小
		frame.setLocation(x, y); // 设置默认大小
	}

}
