
input reader

main
    validate input(file)
    if fail error
    inputbuffer buffer = read file
    nem12 process


        

    public void process(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if line is empty continue

                split = line.split
                switch (header)
                    case 100
                    case 200
                    case 300
                    case 400
                    case 500

                    case 900
                        
                    case default throw error
                split
                // extract
                // only 1 header
                // when encounter 200
                //  - if next is 300, process300(200details)
                // - if next is 200, repeat
                // only 1 footer
                // Process each line as needed
                // output type sql
                System.out.println(line); // Example: simply print each line
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
