package org.unindented.regextester;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Highlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

/**
 * Simple regex tester applet.
 *
 * It's composed of three panels:
 * <ol>
 * <li>The top panel, where the user enters the regex and its flags ({@link RegexPanel}).</li>
 * <li>The left panel, where the user enters the test string ({@link TestPanel}).</li>
 * <li>The right panel, where possible matches are displayed ({@link MatchPanel}).</li>
 * </ol>
 *
 * @author Daniel Perez Alvarez
 */
public class RegexTester extends JApplet
{
    private static final long serialVersionUID = 1L;

    private final Font font = new Font(Font.MONOSPACED, Font.PLAIN, 14);
    private final DefaultHighlightPainter hilitePainter = new DefaultHighlightPainter(null);

    private RegexPanel regexPanel;
    private TestPanel testPanel;
    private MatchPanel matchPanel;

    public String getAppletInfo()
    {
        return "Title: RegexTester v1.0, 22 Sep 2009\n" //
            + "Author: Daniel Perez Alvarez\n" //
            + "Java regex tester.";
    }

    public void init()
    {
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                public void run()
                {
                    initLAF();
                    createGUI();
                }
            });
        }
        catch (Exception e)
        {
            System.err.println("Couldn't create applet.");
            e.printStackTrace();
        }
    }

    private void initLAF()
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
            System.err.println("Couldn't set look & feel.");
            e.printStackTrace();
        }
    }

    private void createGUI()
    {
        regexPanel = new RegexPanel();
        testPanel = new TestPanel();
        matchPanel = new MatchPanel();

        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, testPanel, matchPanel);
        centerSplit.setResizeWeight(0.5);
        centerSplit.setBorder(BorderFactory.createEmptyBorder());

        getContentPane().add(regexPanel, BorderLayout.NORTH);
        getContentPane().add(centerSplit, BorderLayout.CENTER);
    }

    public static void main(final String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                RegexTester applet = new RegexTester();
                applet.initLAF();
                applet.createGUI();

                JFrame frame = new JFrame("Regex Tester");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().add(applet);

                frame.pack();
                frame.setVisible(true);
            }
        });
    }

    /**
     * Top panel, where the user enters the regex and its flags.
     */
    public class RegexPanel extends JPanel
    {
        private static final long serialVersionUID = 1L;

        private final JTextField regexField;
        private final JTextField flagField;
        private final JTextField quotedField;

        public RegexPanel()
        {
            super(new BorderLayout());

            // regex
            regexField = new JTextField();
            regexField.setFont(font);
            // flags
            flagField = new JTextField(6);
            flagField.setFont(font);
            // quoted regex + flags
            quotedField = new JTextField();
            quotedField.setFont(font);
            quotedField.setEditable(false);

            JPanel regexPanel = new JPanel(new BorderLayout());
            regexPanel.add(regexField, BorderLayout.CENTER);

            JPanel flagPanel = new JPanel(new BorderLayout());
            flagPanel.add(new JLabel(" ? "), BorderLayout.WEST);
            flagPanel.add(flagField, BorderLayout.CENTER);

            JPanel quotedPanel = new JPanel(new BorderLayout());
            quotedPanel.add(quotedField, BorderLayout.CENTER);

            add(regexPanel, BorderLayout.CENTER);
            add(flagPanel, BorderLayout.EAST);
            add(quotedPanel, BorderLayout.SOUTH);

            setBorder( //
                BorderFactory.createCompoundBorder( //
                    BorderFactory.createCompoundBorder( //
                        BorderFactory.createTitledBorder("Your regular expression"), //
                        BorderFactory.createEmptyBorder(5, 5, 5, 5) //
                    ), //
                    getBorder() //
                ) //
            );

            // refresh both the quoted regex and the match panel every time the
            // user updates the fields
            DocumentListener listener = new DocumentListener()
            {
                public void insertUpdate(final DocumentEvent e)
                {
                    update();
                }

                public void removeUpdate(final DocumentEvent e)
                {
                    update();
                }

                public void changedUpdate(final DocumentEvent e)
                {
                }

                private void update()
                {
                    String regex = getRegex();
                    updateQuoted(regex);
                    matchPanel.updateRegex(regex);
                }
            };
            regexField.getDocument().addDocumentListener(listener);
            flagField.getDocument().addDocumentListener(listener);
        }

        public String getRegex()
        {
            String regex = regexField.getText();
            String flags = flagField.getText().trim();
            if (flags.length() > 0)
            {
                regex = "(?" + flags + ")" + regex;
            }

            return regex;
        }

        public void updateQuoted(final String regex)
        {
            String charsToQuote = "\b\t\n\f\r\"\'\\";

            StringBuffer buffer = new StringBuffer(regex);
            for (int i = regex.length() - 1; i >= 0; i--)
            {
                if (charsToQuote.indexOf(buffer.charAt(i)) >= 0)
                {
                    buffer.insert(i, '\\');
                }
            }

            quotedField.setText(buffer.toString());
        }
    }

    /**
     * Left panel, where the user enters the test string.
     */
    public class TestPanel extends JPanel
    {
        private static final long serialVersionUID = 1L;

        private final JTextArea testField;

        public TestPanel()
        {
            super(new BorderLayout());

            testField = new JTextArea();
            testField.setFont(font);
            testField.setLineWrap(true);
            testField.setWrapStyleWord(true);

            add(new JScrollPane(testField), BorderLayout.CENTER);

            setBorder( //
                BorderFactory.createCompoundBorder( //
                    BorderFactory.createCompoundBorder( //
                        BorderFactory.createTitledBorder("Your test string"), //
                        BorderFactory.createEmptyBorder(5, 5, 5, 5) //
                    ), //
                    getBorder() //
                ) //
            );

            // refresh the match panel every time the user updates the fields
            DocumentListener listener = new DocumentListener()
            {
                public void insertUpdate(final DocumentEvent e)
                {
                    update();
                }

                public void removeUpdate(final DocumentEvent e)
                {
                    update();
                }

                public void changedUpdate(final DocumentEvent e)
                {
                }

                private void update()
                {
                    matchPanel.updateTest(getTest());
                }
            };
            testField.getDocument().addDocumentListener(listener);
        }

        public String getTest()
        {
            return testField.getText();
        }
    }

    /**
     * Right panel, where possible matches are displayed.
     */
    public class MatchPanel extends JPanel
    {
        private static final long serialVersionUID = 1L;

        private String regex;
        private String test;

        private Pattern pattern;
        private Matcher matcher;

        private final JTextArea resultField;
        private final JTextArea captureField;

        public MatchPanel()
        {
            super(new BorderLayout());

            resultField = new JTextArea();
            resultField.setFont(font);
            resultField.setLineWrap(true);
            resultField.setWrapStyleWord(true);
            resultField.setEditable(false);

            JPanel resultPanel = new JPanel(new BorderLayout());
            resultPanel.add(new JScrollPane(resultField), BorderLayout.CENTER);

            resultPanel.setBorder( //
                BorderFactory.createCompoundBorder( //
                    BorderFactory.createCompoundBorder( //
                        BorderFactory.createTitledBorder("Match result"), //
                        BorderFactory.createEmptyBorder(5, 5, 5, 5) //
                    ), //
                    getBorder() //
                ) //
            );

            captureField = new JTextArea();
            captureField.setFont(font);
            captureField.setLineWrap(true);
            captureField.setWrapStyleWord(true);
            captureField.setEditable(false);

            JPanel capturePanel = new JPanel(new BorderLayout());
            capturePanel.add(new JScrollPane(captureField), BorderLayout.CENTER);

            capturePanel.setBorder( //
                BorderFactory.createCompoundBorder( //
                    BorderFactory.createCompoundBorder( //
                        BorderFactory.createTitledBorder("Match captures"), //
                        BorderFactory.createEmptyBorder(5, 5, 5, 5) //
                    ), //
                    getBorder() //
                ) //
            );

            JSplitPane matchSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, resultPanel, capturePanel);
            matchSplit.setResizeWeight(0.35);
            matchSplit.setBorder(BorderFactory.createEmptyBorder());

            add(matchSplit, BorderLayout.CENTER);
        }

        public void updateRegex(final String regex)
        {
            this.regex = regex;

            try
            {
                pattern = Pattern.compile(this.regex);
                updateTest(this.test);
            }
            catch (Exception e)
            {
                pattern = null;
                resultField.setText(e.getMessage());
            }
        }

        public void updateTest(final String test)
        {
            this.test = test;

            if (pattern != null)
            {
                try
                {
                    matcher = pattern.matcher(this.test);
                    if (!matcher.find())
                    {
                        resultField.setText("No matches.");
                        captureField.setText("");
                    }
                    else
                    {
                        resultField.setText(this.test);
                        Highlighter hilite = resultField.getHighlighter();

                        StringBuffer capture = new StringBuffer();
                        do
                        {
                            hilite.addHighlight(matcher.start(), matcher.end(), hilitePainter);

                            capture.append(matcher.group(0) + "\n");
                            for (int i = 1; i <= matcher.groupCount(); i++)
                            {
                                capture.append("" + i + ". " + matcher.group(i) + "\n");
                            }
                            capture.append("\n");
                        }
                        while (matcher.find());

                        captureField.setText(capture.toString().trim());
                        captureField.setCaretPosition(0);
                    }
                }
                catch (Exception e)
                {
                    resultField.setText(e.getMessage());
                }
            }
        }
    }
}
