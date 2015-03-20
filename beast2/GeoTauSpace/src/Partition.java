/**
 * Created by alex on 21/10/14.
 */

public class Partition {

    Boolean[][] part1;
    Boolean[][] part2;

    part1[0][0]=true;
    part1[0][1]=true;
    part1[0][2]=true;
    part1[1][1]=true;
    part1[1][2]=true;
    part1[2][2]=true;




    part2[0][0]=true;
    part2[0][1]=true;
    part2[1][0]=true;
    part2[1][1]=true;


    public Boolean areCompatible (Boolean[][] part1, Boolean[][] part2) {
        Boolean tmpCompatible = true;
        for (int i = 0; i < part1.length - 1; i++) {
            for (int j = 0; j < part1[i].length - 1; j++) {
                if (!part1[i][j] || part2[i][j]) {
                    tmpCompatible = true;
                } else {tmpCompatible = false;}
            }
        }
        if (tmpCompatible == true) {
            return tmpCompatible;
        } else {tmpCompatible = true}
        for (int i = 0; i < part1.length - 1; i++) {
            for (int j = 0; j < part1[i].length - 1; j++) {
                if (part1[i][j] || !part2[i][j]) {
                    tmpCompatible = true;
                } else {tmpCompatible = false;}
            }
        }
        return tmpCompatible;
    }
}




