#!/usr/bin/env python
#-*- coding: utf-8 -*-
#__author__ = 'zhajiang'

import os,json,time,StringIO,re,logging,traceback

# comment lines as below once using yarn client/cluster to submit job
# os.environ['PYSPARK_SUBMIT_ARGS'] = '--packages org.apache.spark:spark-streaming-kafka-0-8_2.11:2.1.0'
# os.environ['SPARK_LOCAL_DIRS'] = 'C:\work\spark_log'
# os.environ['LOCAL_DIRS'] = os.environ['SPARK_LOCAL_DIRS']
# os.environ['SPARK_WORKER_DIR'] = os.path.join(os.environ['SPARK_LOCAL_DIRS'], 'work')
from pyspark import SparkContext
from pyspark.streaming import StreamingContext
from pyspark.streaming.kafka import KafkaUtils
from datetime import datetime
from optparse import OptionParser
###########################################################################
#save running log in ./cusp_parse_kafka_streaming.log
logging.basicConfig(level=logging.ERROR,
                    #format='%(asctime)s %(filename)s[line:%(lineno)d] %(levelname)s %(message)s'
                    format='%(asctime)s %(levelname)s %(message)s',
                    #datefmt='%a, %d %b %Y %H:%M:%S'
                    filename='cusp_parse_kafka_streaming.log',
                    filemode='w'
)

###########################################################################
#print log on console screen
console = logging.StreamHandler()
console.setLevel(logging.ERROR)
formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
console.setFormatter(formatter)
logging.getLogger('').addHandler(console)
logging.getLogger("py4j").setLevel(logging.ERROR)
###########################################################################

def tellSipHost(str):
    val = ''
    start = str.find('<sip:')
    end = str.find('>')
    if start!=-1 and end!=-1:
        val = str[start+5:end]
        idx = val.find(';')
        if idx!=-1:
            val = val[0:idx]
    return val

def getFileInfo(hdfsOutDir):
    now = datetime.now()
    nowStr = str(now)
    year,mon,day,min = nowStr[0:4],nowStr[5:7],nowStr[8:10],now.strftime('%Y%m%d%H%M')
    dateDir = '%s%s%s' %(year,mon,day)
    fileInfo = '%s/%s/log_%s_%s_%s_%s' %(hdfsOutDir,dateDir,year,mon,day,min)
    return fileInfo

# only for test purpose
def printLog(line):
    print '--line-beg---'
    print line
    print '--line-end---'
    return line

def processLine(line, serverIP):
    try:
        line = processBlock(line, serverIP)
    except Exception, err:
        traceback.print_exc()
    if line is not None:
        return line
    else:
        return None

def main():
    '''
    :params hdfsOutDir:  'hdfs://10.224.243.130:8020/user/admin/cusp/'
    :params checkpoint_dir: checkoutpoint for recover data
    :params brokers: 'sj1-kafka-s.webex.com:9092' 
    :params topic: 'sj1_logstash_all_clp'
    '''    
    options = OptionParser(usage='%prog log [options]', description='Python streaming script to analysis the cusp logs')
    options.add_option('-d', '--dest', type='string', default='', help='')
    opts, args = options.parse_args()
    if len(args) < 3:
        options.print_help()
        return

    brokers=args[0]
    topic=args[1]
    hdfsOutDir=args[2]
    if len(args) == 4:
        checkpoint_dir = args[3]
    else:
        checkpoint_dir = 'checkpoint-cusp-hdfs'

    serverIP = '127.0.0.1'
    sc = SparkContext(appName='Streamly_Sink_Cusp_Logs_From_Kafka_To_Hdfs')

    # sc.setSystemProperty('spark.executor.memory','2g')
    # sc.setSystemProperty('spark.driver.memory','2g')
    # sc.setSystemProperty('spark.python.worker.memory ','2g')
    # sc.setSystemProperty('spark.streaming.backpressure.enabled','true')
    # sc.setSystemProperty('spark.streaming.stopGracefullyOnShutdown','true')
    # sc.setSystemProperty('spark.streaming.unpersist','true')
    # sc.setSystemProperty('spark.io.compression.codec','org.apache.spark.io.LZ4CompressionCodec')
    # sc.setSystemProperty('spark.streaming.kafka.maxRatePerPartition','10') 
    sc.setLogLevel("ERROR")

    ssc = StreamingContext(sc, 1) # 1 second window
    ssc.checkpoint(checkpoint_dir)
    while True:
        try:
            kafkaStream = KafkaUtils.createDirectStream(ssc, [topic], {"metadata.broker.list": brokers})
            #kafkaStream = ssc.textFileStream('file:///C:/work/json')
            break
        except:
            print('Waiting on kafka...')
            time.sleep(1)

    # apply filter 
    def func_filter(line):
        try:
            if (line.get('type') == 'CUSP' and line.get('sip_From').find('vmr_mct_test') != -1 and line.get('sip_To').find('vmr_mct_test') != -1):
                return True
            else:
                return False
        except Exception, err:
            traceback.print_exc()
            return False

    # apply process
    def func_process(line): return processLine(line, "127.0.0.1")

    # apply filter and process
    def parseLineAsCsv(line):
        isCuspLine = func_filter(line)
        if isCuspLine is True:
            line = func_process(line.get('message'))
            return line
        else:
            return None

    '''
    ↓-------Spark Streaming Recived Data samples as below-------↓
    [(None,{"type": "CUSP","@version":"1","@timestamp": "2017-05-03T14:05:12.965Z","host": "ptsj8tsp007.webex.com","message": "xxxxxx"})
    (None,{"type": "CUSP","@version":"1","@timestamp": "2017-05-03T14:05:12.965Z","host": "ptsj8tsp007.webex.com","message": "xxxxxx"})
    (None,{"type": "CUSP","@version":"1","@timestamp": "2017-05-03T14:05:12.965Z","host": "ptsj8tsp007.webex.com","message": "xxxxxx"})]
    '''
    # Option#1 ====== for one step
    # lines = kafkaStream.map(lambda line: parseLineAsCsv(json.loads(line[1]))).filter(
    #     lambda line: line is not None).repartition(1).saveAsTextFiles(getFileInfo(),'csv')#.saveAsTextFiles('C:\work\json_dest\mytest', 'csv')
    
    # Option#2 ====== for multi step
    # lines = kafkaStream.filter(lambda line: func_filter(json.loads(line[1])).map(lambda line: json.loads(line[1]).get(
    #     'message')).map(func_process).filter(lambda line: line is not None).map(lambda line: printLog(line))

    # Test Local Files###
    lines = kafkaStream.filter(lambda line: func_filter(json.loads(line))).map(lambda line: json.loads(line).get('message')).map(
        func_process).filter(lambda line: line is not None)#.repartition(1).saveAsTextFiles('C:\work\json_dest\mytest','txt')

    # lines.pprint()
    ssc.start()
    ssc.awaitTermination()
    #ssc.awaitTerminationOrTimeout(10)
    #ssc.stop()

####################################
##### logic for parse log begin ####
####################################

####################################
##### Start Program #####
####################################

if __name__ == '__main__':
    logging.info("reading and parseing cusp logs -- Getting started...")
    main()
