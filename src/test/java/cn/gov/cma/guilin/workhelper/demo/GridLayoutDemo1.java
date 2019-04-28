package cn.gov.cma.guilin.workhelper.demo;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * 
 * Created by it 
 * Created in 2019年4月26日 
 * Description: GridLayout将整个空间划分成若干行乘若干列的网络区域，组件就放于这些小区域内。
 */
public class GridLayoutDemo1 {
	static final int WIDTH = 800;
	static final int HEIGHT = 600;

	public static void main(String[] args) {
		JFrame jf = new JFrame("测试程序");
		jf.setSize(WIDTH, HEIGHT);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel contentPane = new JPanel();
		jf.setContentPane(contentPane);
		JButton b1 = new JButton("港币");
		JButton b2 = new JButton("人民币");
		JButton b3 = new JButton("美元");
		JButton b4 = new JButton("欧元");
		JButton b5 = new JButton("英镑");
		JButton b6 = new JButton("主板");
		JButton b7 = new JButton("内存");
		JButton b8 = new JButton("硬盘");
		JButton b9 = new JButton("显示器");

		GridLayout gird = new GridLayout(3, 3); // 创建一个
												// GridLayout布局管理器对象，将之行数设为3，列数设为3,并且将之作为中间容器的布局管理器
		contentPane.setLayout(gird);

		contentPane.add(b1); // 将九个普通按钮组件一一添加到中间容器中
		contentPane.add(b2);
		contentPane.add(b3);
		contentPane.add(b4);
		contentPane.add(b5);
		contentPane.add(b6);
		contentPane.add(b7);
		contentPane.add(b8);
		contentPane.add(b9);
		// jf.pack();
		jf.setVisible(true);
	}
}
