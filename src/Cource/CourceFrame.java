﻿package Cource;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import Account.LoginFrame;
import Account.LoginOption;
import Enrollment.EnrollBtnPanel;
import Enrollment.EnrollmentPanel;
import Framework.ICApply;
import Framework.ICBasket;
import Framework.Launcher;
import main.Connector;
import main.CurrentUser;

public class CourceFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	// UI
	private GreetPanel greetPanel;
	public SelectionPanel selectionPanel;
	private EnrollBtnPanel enrollBtnPanel;
	private EnrollmentPanel enrollmentPanel;
	private ActionListener actionListener;
	private MouseListener mouseListener;

	private String id; // 아이디
	private int credit, basketCredit, applyCredit;

	private LoginOption loginOption;

	// 선택된 패널
	private boolean lecture = false;
	private boolean basket = false;
	private boolean apply = false;

	// 선택한 강좌 리스트
	Vector<ELecture> lectures;
	Vector<ELecture> storedLectures;
	
	private int selectCredit;

	private static final Class<ICApply> icApplyClass = ICApply.class;
	private static final Class<ICBasket> icBasketClass = ICBasket.class;
	private static Method applyShow;
	private static Method applyAdd;
	private static Method applyDelete;
	private static Method basketShow;
	private static Method basketAdd;
	private static Method basketDelete;
	
	static {
		try {
			basketAdd = icBasketClass.getMethod("add", Vector.class, Vector.class, String.class);
			basketShow = icBasketClass.getMethod("show", String.class);
			basketDelete = icBasketClass.getMethod("delete", Vector.class, String.class);
			applyAdd = icApplyClass.getMethod("add", Vector.class, Vector.class, String.class);
			applyShow = icApplyClass.getMethod("show", String.class);
			applyDelete = icApplyClass.getMethod("delete", Vector.class, String.class);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	
	public CourceFrame() {

		this.id = CurrentUser.id;
		
		this.setTitle("명지대학교 수강신청 시스템");
		// 아이콘 이미지
		File icon = new File("image/icon.gif");
		try {
			Image img = ImageIO.read(icon);
			setIconImage(img);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		this.actionListener = new ActionHandler();
		this.mouseListener = new MouseHandler();

		this.greetPanel = new GreetPanel(actionListener);
		this.greetPanel.setPreferredSize(new Dimension(1000, 50));
		this.selectionPanel = new SelectionPanel(mouseListener);
		this.selectionPanel.setPreferredSize(new Dimension(1000, 370));
		this.enrollmentPanel = new EnrollmentPanel(id, mouseListener);
		this.enrollmentPanel.setPreferredSize(new Dimension(600, 460));
		this.enrollBtnPanel = new EnrollBtnPanel(actionListener);
		this.enrollBtnPanel.setPreferredSize(new Dimension(1000, 50));
		
		this.setLayout(new FlowLayout());
		
		this.add(greetPanel);
		this.add(selectionPanel);
		this.add(enrollmentPanel);
		this.add(enrollBtnPanel);

		
		this.setSize(1200, 1000); // x,y축
		this.setMinimumSize(new Dimension(650, 0));
		this.setLocationRelativeTo(null);
		
		credit = CurrentUser.credit;
		basketCredit = CurrentUser.basket;
		applyCredit = CurrentUser.apply;
		this.enrollmentPanel.basket.setText("장바구니 (가능학점: " + (credit-basketCredit+7) + ")");
		this.enrollmentPanel.apply.setText("수강신청 (가능학점: " + (credit-applyCredit) + ")");
	}

	public void addLectures(String opt) {
		selectCredit = 0;
		
		try {
			if (opt.equals("basket")) {
				if (lecture) {
					lectures = this.selectionPanel.lecture.getSelectedLectures();
					
					for(ELecture lecture: lectures) {
						selectCredit+=lecture.getCredit();
					}
					
					if(credit-basketCredit-selectCredit+7<0) {
						JOptionPane.showMessageDialog(null, "최대 미리담기가능한 학점을 초과하였습니다."
								+ "\n(해당 과목을 신청하시려면 다른 과목을 삭제하십시오.)", "학점제한", JOptionPane.ERROR_MESSAGE);
					}
					
				} else if (apply) {
					lectures = this.enrollmentPanel.applyTable.getSelectedLectures();
					for(ELecture lecture: lectures) {
						selectCredit+=lecture.getCredit();
					}
					
					if(credit-basketCredit-selectCredit+7<0) {
						JOptionPane.showMessageDialog(null, "최대 미리담기가능한 학점을 초과하였습니다."
								+ "\n(해당 과목을 신청하시려면 다른 과목을 삭제하십시오.)", "학점제한", JOptionPane.ERROR_MESSAGE);
					} else {
						this.deleteLectures();
					}
				}
                Vector<ELecture> applyLectures = (Vector<ELecture>) Connector.invoke(new Launcher(icApplyClass.getSimpleName(), applyShow.getName(), applyShow.getParameterTypes(), new Object[]{id})); // 신청 목록 가져오기
				
				if(credit-basketCredit-selectCredit+7>=0 && 
						(Boolean) Connector.invoke(new Launcher(icBasketClass.getSimpleName(), basketAdd.getName(), basketAdd.getParameterTypes(), new Object[]{lectures, applyLectures, id}))) { // 장바구니 추가함수
					JOptionPane.showMessageDialog(null, "선택한 강좌 중에 이미 신청하거나 미리담은 강좌가 있습니다."
							+ "\n(중복되지 않은 강좌가 있다면 정상적으로 추가됩니다.)", "중복된 강의 존재", JOptionPane.ERROR_MESSAGE);
				};
                storedLectures = (Vector<ELecture>) Connector.invoke(new Launcher(icBasketClass.getSimpleName(), basketShow.getName(), basketShow.getParameterTypes(), new Object[]{id})); // 추가 결과 리턴
				this.enrollmentPanel.basketTable.refresh(storedLectures);
				
				// 담기가능학점 출력
				basketCredit = CurrentUser.basket;
				this.enrollmentPanel.basket.setText("장바구니 (가능학점: " + (credit-basketCredit+7) + ")");

			} else if (opt.equals("apply")) {
				if (lecture) {
					lectures = this.selectionPanel.lecture.getSelectedLectures();
					for(ELecture lecture: lectures) {
						selectCredit+=lecture.getCredit();
					}
					
					if(credit-applyCredit-selectCredit<0) {
						JOptionPane.showMessageDialog(null, "최대 수강신청가능한 학점을 초과하였습니다."
								+ "\n(해당 과목을 신청하시려면 다른 과목을 삭제하십시오.)", "학점제한", JOptionPane.ERROR_MESSAGE);
					}
				} else if (basket) {
					lectures = this.enrollmentPanel.basketTable.getSelectedLectures();
					for(ELecture lecture: lectures) {
						selectCredit+=lecture.getCredit();
					}
					
					if(credit-applyCredit-selectCredit<0) {
						JOptionPane.showMessageDialog(null, "최대 수강신청가능한 학점을 초과하였습니다."
								+ "\n(해당 과목을 신청하시려면 다른 과목을 삭제하십시오.)", "학점제한", JOptionPane.ERROR_MESSAGE);
					} else {
						this.deleteLectures();
					}
					
				}
                Vector<ELecture> basketLectures = (Vector<ELecture>) Connector.invoke(new Launcher(icBasketClass.getSimpleName(), basketShow.getName(), basketShow.getParameterTypes(), new Object[]{id})); // 장바구니 리스트 가져오기
				
				if (credit-applyCredit-selectCredit>=0 && 
						(Boolean) Connector.invoke(new Launcher(icApplyClass.getSimpleName(), applyAdd.getName(), applyAdd.getParameterTypes(), new Object[]{lectures, basketLectures, id}))){ // 신청목록 추가함수
				JOptionPane.showMessageDialog(null, "선택한 강좌 중에 이미 신청하거나 미리담은 강좌가 있습니다."
						+ "\n(중복되지 않은 강좌가 있다면 정상적으로 추가됩니다.)", "중복된 강의 존재", JOptionPane.ERROR_MESSAGE);
			};
				
            	storedLectures = (Vector<ELecture>) Connector.invoke(new Launcher(icApplyClass.getSimpleName(), applyShow.getName(), applyShow.getParameterTypes(), new Object[]{id})); // 추가 결과 리턴
				this.enrollmentPanel.applyTable.refresh(storedLectures);
				
				// 수강가능학점 출력
				applyCredit = CurrentUser.apply;
				this.enrollmentPanel.apply.setText("수강신청 (가능학점: " + (credit-applyCredit) + ")");
			}

		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void deleteLectures() {
		if (basket) {
			lectures = this.enrollmentPanel.basketTable.getSelectedLectures();
			try {
				Connector.invoke(new Launcher(icBasketClass.getSimpleName(), basketDelete.getName(), basketDelete.getParameterTypes(), new Object[]{lectures, id}));
				storedLectures = (Vector<ELecture>) Connector.invoke(new Launcher(icBasketClass.getSimpleName(), basketShow.getName(), basketShow.getParameterTypes(), new Object[]{id}));
				this.enrollmentPanel.basketTable.refresh(storedLectures);
				
				// 담기가능학점 출력
				basketCredit = CurrentUser.basket;
				this.enrollmentPanel.basket.setText("장바구니 (가능학점: " + (credit-basketCredit+7) + ")");
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (apply) {
			lectures = this.enrollmentPanel.applyTable.getSelectedLectures();
			try {
				Connector.invoke(new Launcher(icApplyClass.getSimpleName(), applyDelete.getName(), applyDelete.getParameterTypes(), new Object[]{lectures, id}));
				storedLectures = (Vector<ELecture>) Connector.invoke(new Launcher(icApplyClass.getSimpleName(), applyShow.getName(), applyShow.getParameterTypes(), new Object[]{id}));
				this.enrollmentPanel.applyTable.refresh(storedLectures);
				
				// 신청가능학점 출력
				applyCredit = CurrentUser.apply;
				this.enrollmentPanel.apply.setText("수강신청 (가능학점: " + (credit-applyCredit) + ")");
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void setFocus(Object source) {
		if (source == this.selectionPanel.lecture) {
			this.enrollmentPanel.basketTable.clearSelection();
			this.enrollmentPanel.applyTable.clearSelection();
			lecture = true;
			basket = false;
			apply = false;
		} else if (source == this.enrollmentPanel.basketTable) {
			this.selectionPanel.lecture.clearSelection();
			this.enrollmentPanel.applyTable.clearSelection();
			lecture = false;
			basket = true;
			apply = false;
		} else if (source == this.enrollmentPanel.applyTable) {
			this.selectionPanel.lecture.clearSelection();
			this.enrollmentPanel.basketTable.clearSelection();
			lecture = false;
			basket = false;
			apply = true;
		}

	}

	public void logout() {
        Connector.disconnect();
		try {
            Connector.initialize();
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            loginOption = new LoginOption();
			loginOption.set("null", "null", "null");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dispose();
    }
	
	public void showUserInfo() {
		UserInfoFrame userInfoFrame = new UserInfoFrame();
		userInfoFrame.setVisible(true);
	}

	private class ActionHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			switch (e.getActionCommand()) {
            case "basket":
                addLectures("basket");
                break;
            case "apply":
                addLectures("apply");
                break;
            case "delete":
                deleteLectures();
                break;
            case "logout":
                logout();
                break;
            case "info":
                showUserInfo();	
			}
		}

	}

	private class MouseHandler implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			setFocus(e.getSource());
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			setFocus(e.getSource());
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub

		}

	}
}
