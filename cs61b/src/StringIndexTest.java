  
public class StringIndexTest{

	public static void main(String[] args) {
		
		String originalString = args[0]; //"abcedefg"
		String subString =  args[1]; //"def"
		
		int result = stringSearch(originalString,subString);
		System.out.println((result == -1? "Error": "Result = "+result));
	}
	
    public static int stringSearch(String baseString, String subString){
        if((baseString==null) ||(subString==null)){
            return -1;
        }
        
      int baseLength = baseString.length();
        int subStringLength =subString.length();
        char baseStringCharArray[] = baseString.toCharArray();
        char subStringCharArray[] =subString.toCharArray();
        
        if(subStringLength > baseLength){
            return -1;
        }
        int startIndex=0,endIndex;
        
        for(int i=0;i < baseLength;i++){
                if(baseStringCharArray[i]== subStringCharArray[0]){
                      startIndex=endIndex=i++;
                      for(int j=1;(j<subStringLength)&&(i<baseLength);j++,i++){
                            if(baseStringCharArray[i]== subStringCharArray[j])
                                  endIndex=i;
                            else
                                  break;                            
                      }
                      if(((endIndex+1)-startIndex)==subStringLength){
                           // System.out.println("Match found");
                            return startIndex;
                      }
                      i=startIndex;
                }
        }
        return -1;
 }

}
  