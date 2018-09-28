package com.relay2.Form;

import com.android.ddmlib.IDevice;
import com.relay2.ScreenCapture.OperateAndroid;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Created by zhongxia on 10/25/16.
 */
public class ControlPage extends JFrame {
    public JPanel panel1;
    public JLabel lblImage;
    private JTextPane tpCommand;
    private JButton btnExec;
    private JButton btnClean;
    private IDevice _device = null;
    private static ArrayList<OperateAndroid> _oas = null;
    private double zoom = 3;
    private int width = (int) (1536 / zoom);
    private int height = (int) (2048 / zoom);
    private ImageThread it = null;
    private Boolean recordFlag = false; //是否录制脚本
    private String command = "";  //命令

    public ControlPage(IDevice[] device, ArrayList<OperateAndroid> oas) throws HeadlessException {
        _device = device[0];
        _oas = oas;
        lblImage.setSize(width, height);
        initEvent();

        it = new ImageThread(lblImage, _device, width, height);

        if (!it.isAlive()) {
            it.start();
        }
    }

    /**
     * 停止线程
     */
    public void stopThread() {
        if (it != null && it.isAlive()) {
            it.stop();
            System.out.println("中断操作设备的线程!");
        }
    }

    /**
     * 初始化操作事件
     */
    public void initEvent() {
        lblImage.addMouseListener(new R2MouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    int x = (int) (e.getX() * zoom);
                    int y = (int) (e.getY() * zoom);
                    for (OperateAndroid oa : _oas) {
                        oa.touchUp((int) (e.getX() * zoom), (int) (e.getY() * zoom));
                    }
                    command += "touch up " + x + " " + y + "\n";
                    setCommandText();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    int x = (int) (e.getX() * zoom);
                    int y = (int) (e.getY() * zoom);
                    for (OperateAndroid oa : _oas) {
                        oa.touchDown(x, y);
                    }
                    command += "touch down " + x + " " + y + "\n";
                    setCommandText();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        lblImage.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseMoved(MouseEvent e) {
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                try {
                    for (OperateAndroid oa : _oas) {
                        oa.touchMove((int) (e.getX() * zoom), (int) (e.getY() * zoom));
                    }
                    System.out.println("move");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        lblImage.addMouseWheelListener(new MouseWheelListener() {

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                for (OperateAndroid oa : _oas) {
                    if (e.getWheelRotation() == 1) {
                        oa.press("KEYCODE_DPAD_DOWN");
                        command += "press KEYCODE_DPAD_DOWN \n";
                    } else if (e.getWheelRotation() == -1) {
                        oa.press("KEYCODE_DPAD_UP");
                        command += "press KEYCODE_DPAD_UP \n";
                    }
                }
            }
        });

        this.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                for (OperateAndroid oa : _oas) {
                    switch (code) {
                        case KeyEvent.VK_BACK_SPACE:
                            oa.press("KEYCODE_DEL");
                            break;
                        case KeyEvent.VK_SPACE:
                            oa.press("KEYCODE_SPACE");
                            break;
                        case KeyEvent.VK_DELETE:
                            oa.press("KEYCODE_FORWARD_DEL");
                            break;
                        case KeyEvent.VK_UP:
                            oa.press("KEYCODE_DPAD_UP");
                            break;
                        case KeyEvent.VK_DOWN:
                            oa.press("KEYCODE_DPAD_DOWN");
                            break;
                        case KeyEvent.VK_LEFT:
                            oa.press("KEYCODE_DPAD_LEFT");
                            break;
                        case KeyEvent.VK_RIGHT:
                            oa.press("KEYCODE_DPAD_RIGHT");
                            break;
                        case KeyEvent.VK_ENTER:
                            oa.press("KEYCODE_ENTER");
                            break;
                        case KeyEvent.VK_CONTROL:
                            break;
                        case KeyEvent.VK_ALT:
                            break;
                        case KeyEvent.VK_SHIFT:
                            break;
                        default:
                            oa.type(e.getKeyChar());
                    }
                }

            }
        });

        /**
         * 执行录制的操作
         * */
        btnExec.addMouseListener(new R2MouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                String[] commands = command.split("\n");
                for (int i = 0; i < commands.length; i++) {
                    try {
                        for (OperateAndroid oa : _oas) {
                            oa.shell(commands[i]);
                        }
                        /**
                         * touch down  和 touch up  两个步骤和在一起,才是一个完整的操作
                         * 每一个操作之间间隔2s
                         */
                        if (commands[i].contains("touch up")) {
                            Thread.sleep(2000);
                        }

                    } catch (InterruptedException ev) {

                    }
                }
            }
        });

        btnClean.addMouseListener(new R2MouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                tpCommand.setText("");
            }
        });
    }

    /**
     * 设置文本域显示命令
     */
    public void setCommandText() {
        tpCommand.setText(command);
    }
}
