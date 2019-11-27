﻿package main;

import java.io.FileNotFoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.JFrame;

import Account.LoginFrame;
import Cource.CourceFrame;
import Framework.ICLogin;

public class Main {

	private static String[] option;
	
	static ICLogin iCLogin = null;
	
	static LoginFrame loginFrame;
	static CourceFrame courceFrame;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String host = (args.length < 1) ? null : args[0];
			
		try {
			Constant.registry = LocateRegistry.getRegistry(host);
			iCLogin = (ICLogin) Constant.registry.lookup("iCLogin");
			option = iCLogin.getOption();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(option[0].equals("자동로그인")) {
			courceFrame = new CourceFrame(option[1], option[2]);
			courceFrame.setVisible(true);
			courceFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}else {
			loginFrame = new LoginFrame(iCLogin);
			loginFrame.setVisible(true);
			loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // close button func//
		}
		
	}

}
