This implementation of Spreadsheet Calculator consists of three classes:

1)	Spreadsheet
The main class used for reading user input and displaying the output.
2)	SpreadsheetEvaluator
This class is used to fill in the values for the individual cells in the spreadsheet. I use a hash table to maintain the mapping between the cell (in terms of rows and cols) and the evaluated value of that cell. 
3)	RPNExpressionEvaluator
This class evaluates all expressions in RPN and returns the value of the RPN expression.

If I had more time, I would do the following:
1)	A Cleaner implementation would be to use a Graph and represent each cell in the spreadsheet as a vertex and create edges between the various other dependent cells. Once the graph is constructed, doing a DFS on the current cell would take care of calculating the current cell’s value and all the dependent values along the way.
2)	Add JUnit test cases for the various methods to test for more scenarios and identify test cases that break the flow. 

